package com.ejafi.egmd.content.block;

import com.ejafi.egmd.core.registration.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Optional;

/**
 * Orchestrates a chain of {@link PortalSearchStrategy} instances to find a suitable
 * {@link BlockPos} in the destination level at which a portal can be placed.
 *
 * <p>Strategies are tried in order; the first non-empty result wins.</p>
 */
final class PortalPositionFinder {

    /**
     * Default chain: spiral outward, fall back to straight-down scan.
     */
    static final PortalPositionFinder DEFAULT = new PortalPositionFinder(
            List.of(new SpiralSearch(), new FallbackSearch()));

    private final List<PortalSearchStrategy> strategies;

    PortalPositionFinder(List<PortalSearchStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    /**
     * Returns the best position for a new portal near {@code targetPos}, or empty if no
     * strategy found a suitable location.
     */
    Optional<BlockPos> find(ServerLevel destLevel, BlockPos targetPos, MiningPortalBlock block) {
        BlockPos center = adjustedStartPos(destLevel, targetPos, block);
        loadChunks(destLevel, center);

        int maxY = destLevel.getMaxBuildHeight() - 1;
        for (PortalSearchStrategy strategy : strategies) {
            Optional<BlockPos> result = strategy.search(destLevel, center, maxY);
            if (result.isPresent()) return result;
        }
        return Optional.empty();
    }

    /**
     * Snaps the Y coordinate to the dimension surface when entering a mining dimension.
     */
    private static BlockPos adjustedStartPos(ServerLevel destLevel, BlockPos targetPos, MiningPortalBlock block) {
        if (ModDimensions.ALL_MINING_DIMENSIONS.contains(destLevel.dimension())) {
            return new BlockPos(targetPos.getX(), block.getSurfaceY(), targetPos.getZ());
        }
        return targetPos;
    }

    /**
     * Force-generates the 3×3 chunk neighborhood around {@code center} so that heightmap
     * queries return real values even on first visit to the dimension.
     */
    private static void loadChunks(ServerLevel level, BlockPos center) {
        int cx = center.getX() >> 4;
        int cz = center.getZ() >> 4;
        for (int x = cx - 1; x <= cx + 1; x++) {
            for (int z = cz - 1; z <= cz + 1; z++) {
                level.getChunk(x, z);
            }
        }
    }
}
