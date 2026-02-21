package com.ejafi.egmd.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Optional;

/**
 * Searches for a portal placement position by spiraling outward from the target XZ,
 * descending from the surface at each column, and choosing the nearest valid candidate.
 */
final class SpiralSearch implements PortalSearchStrategy {

    @Override
    public Optional<BlockPos> search(ServerLevel level, BlockPos center, int maxY) {
        WorldBorder border = level.getWorldBorder();

        for (BlockPos.MutableBlockPos candidate : BlockPos.spiralAround(center, 16, Direction.EAST, Direction.SOUTH)) {
            if (!border.isWithinBounds(candidate)) continue;

            int y = Math.min(maxY, level.getHeight(Heightmap.Types.MOTION_BLOCKING, candidate.getX(), candidate.getZ()));
            candidate.setY(y);

            if (PortalSearchStrategy.canPlaceAt(level, candidate)) {
                return Optional.of(candidate.immutable());
            }
        }
        return Optional.empty();
    }
}
