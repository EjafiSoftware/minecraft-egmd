package com.ejafi.egmd.content.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;

/**
 * Strategy for locating the origin at which {@link PortalStructureBuilder} can build a portal.
 *
 * <p>Implementations must be stateless: the same instance may be called concurrently and must
 * not retain any per-call state between invocations.</p>
 */
@FunctionalInterface
interface PortalSearchStrategy {

    /**
     * Attempts to find a portal origin near {@code center}.
     *
     * @param level  the destination level to search in
     * @param center the target position; in mining dimensions its Y is the flat surface
     * @param axis   the axis the portal will be built along
     * @return the chosen origin, or empty if no suitable location was found
     */
    Optional<BlockPos> search(ServerLevel level, BlockPos center, Axis axis);
}
