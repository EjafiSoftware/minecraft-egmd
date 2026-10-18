package com.ejafi.egmd.dimension;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.phys.Vec3;

final class ClearSpaceFinder {
    private final CollisionGetter level;
    private final Entity entity;
    private final EntityDimensions dimensions;

    ClearSpaceFinder(CollisionGetter level, Entity entity) {
        this.level = level;
        this.entity = entity;
        this.dimensions = entity.getDimensions(entity.getPose());
    }

    Optional<BlockPos> findAbove(BlockPos start) {
        int highestFeetY = level.getMaxBuildHeight() - Mth.ceil(dimensions.height());
        for (BlockPos pos = start; pos.getY() <= highestFeetY; pos = pos.above()) {
            if (level.noCollision(entity, dimensions.makeBoundingBox(Vec3.atBottomCenterOf(pos)))) {
                return Optional.of(pos);
            }
        }
        return Optional.empty();
    }
}
