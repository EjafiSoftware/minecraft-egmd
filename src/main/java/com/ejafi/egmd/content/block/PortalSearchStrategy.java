package com.ejafi.egmd.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Strategy for locating a candidate {@link BlockPos} in which a portal can be placed.
 *
 * <p>Implementations must be stateless: the same instance may be called concurrently and must
 * not retain any per-call state between invocations.</p>
 *
 * <p>Implementations should treat {@code center} as read-only and create their own mutable
 * copy when they need to traverse positions.</p>
 */
@FunctionalInterface
interface PortalSearchStrategy {

    /**
     * Attempts to find a valid portal placement position near {@code center}.
     *
     * @param level  the destination level to search in
     * @param center the starting reference position (read-only)
     * @param maxY   the highest Y that may be considered
     * @return the chosen position, or empty if no suitable location was found
     */
    Optional<BlockPos> search(ServerLevel level, BlockPos center, int maxY);

    /**
     * Returns {@code true} if a portal base block can be placed at {@code pos}.
     */
    static boolean canPlaceAt(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced();
    }
}
