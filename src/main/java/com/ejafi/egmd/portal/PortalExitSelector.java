package com.ejafi.egmd.portal;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class PortalExitSelector {
    private static final double BURN_CHECK_INSET = 1.0E-6;

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
        AABB box = dimensions.makeBoundingBox(position);
        return level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
                && level.noCollision(entity, box)
                && level.getBlockStatesIfLoaded(box.deflate(BURN_CHECK_INSET)).noneMatch(PortalExitSelector::burns);
    }

    private static boolean burns(BlockState state) {
        return state.is(BlockTags.FIRE) || state.is(Blocks.LAVA);
    }
}
