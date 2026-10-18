package com.ejafi.egmd.portal;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.Vec3;

final class PortalExitSelector {
    private final ServerLevel level;
    private final Entity entity;
    private final EntityDimensions dimensions;

    PortalExitSelector(ServerLevel level, Entity entity) {
        this.level = level;
        this.entity = entity;
        this.dimensions = entity.getDimensions(entity.getPose());
    }

    PortalExit select(PortalFrame frame) {
        for (PortalExit candidate : List.of(PortalExit.computeFront(frame), PortalExit.computeBack(frame))) {
            if (isSafeArrival(candidate.position())) {
                return candidate;
            }
        }

        PortalExit inside = PortalExit.computeInside(frame);
        return new PortalExit(
                PortalShape.findCollisionFreePosition(inside.position(), level, entity, dimensions), inside.yRot());
    }

    private boolean isSafeArrival(Vec3 position) {
        BlockPos floor = BlockPos.containing(position).below();
        return level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
                && level.noCollision(entity, dimensions.makeBoundingBox(position));
    }
}
