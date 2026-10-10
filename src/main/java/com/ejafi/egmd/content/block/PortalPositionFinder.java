package com.ejafi.egmd.content.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.border.WorldBorder;

/**
 * Finds where a new portal goes in the destination level: the nearest open site from
 * {@link SpiralSearch}, or failing that a {@link ForcedPlacement} at the target.
 */
final class PortalPositionFinder {
    private static final PortalSearchStrategy OPEN_SEARCH = new SpiralSearch();
    private static final PortalSearchStrategy FORCED_PLACEMENT = new ForcedPlacement();

    private PortalPositionFinder() {}

    /**
     * Where to build a portal, and whether it was forced into terrain.
     *
     * @param origin the portal's origin block
     * @param forced true if the site was not open and the builder must clear the exit space
     */
    record PortalSite(BlockPos origin, boolean forced) {}

    /**
     * Returns the site for a new portal near {@code targetPos}, or empty if no site can be used
     * without destroying a container, another portal, or an unbreakable block.
     *
     * @param targetPos the target position; in mining dimensions its Y must already be the flat surface
     */
    static Optional<PortalSite> find(ServerLevel destLevel, BlockPos targetPos, Axis axis) {
        BlockPos center = clampInsideBorder(destLevel.getWorldBorder(), targetPos);
        loadChunks(destLevel, center);

        return OPEN_SEARCH.search(destLevel, center, axis)
                .map(origin -> new PortalSite(origin, false))
                .or(() -> FORCED_PLACEMENT.search(destLevel, center, axis).map(origin -> new PortalSite(origin, true)));
    }

    /**
     * Moves the center far enough inside the world border that a portal footprint built on it fits.
     */
    private static BlockPos clampInsideBorder(WorldBorder border, BlockPos pos) {
        int minX = Mth.ceil(border.getMinX()) + 1;
        int maxX = Mth.floor(border.getMaxX()) - MiningPortalShape.MIN_WIDTH - 1;
        int minZ = Mth.ceil(border.getMinZ()) + 1;
        int maxZ = Mth.floor(border.getMaxZ()) - MiningPortalShape.MIN_WIDTH - 1;
        if (minX > maxX || minZ > maxZ) return pos;
        return new BlockPos(Mth.clamp(pos.getX(), minX, maxX), pos.getY(), Mth.clamp(pos.getZ(), minZ, maxZ));
    }

    /**
     * Force-generates every chunk the spiral and its footprints can touch, so heightmap queries
     * return real values even on the first visit to the dimension.
     */
    private static void loadChunks(ServerLevel level, BlockPos center) {
        int reach = SpiralSearch.RADIUS + MiningPortalShape.MIN_WIDTH;
        for (int x = (center.getX() - reach) >> 4; x <= (center.getX() + reach) >> 4; x++) {
            for (int z = (center.getZ() - reach) >> 4; z <= (center.getZ() + reach) >> 4; z++) {
                level.getChunk(x, z);
            }
        }
    }
}
