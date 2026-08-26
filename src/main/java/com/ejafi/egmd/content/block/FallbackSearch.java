package com.ejafi.egmd.content.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Last-resort search: scans straight down from the surface at the center XZ and returns
 * the first valid position. Used when {@link SpiralSearch} finds nothing.
 */
final class FallbackSearch implements PortalSearchStrategy {

    @Override
    public Optional<BlockPos> search(ServerLevel level, BlockPos center, int maxY) {
        BlockPos.MutableBlockPos pos = center.mutable();
        int surfaceY = Math.min(maxY, level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()));
        for (int y = surfaceY; y > level.getMinBuildHeight(); --y) {
            pos.setY(y);
            if (PortalSearchStrategy.canPlaceAt(level, pos)) return Optional.of(pos.immutable());
        }
        return Optional.empty();
    }
}
