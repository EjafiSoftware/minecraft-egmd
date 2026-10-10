package com.ejafi.egmd.content.block;

import com.ejafi.egmd.core.registration.ModDimensions;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;

/**
 * Last resort when {@link SpiralSearch} finds no open site: the portal is forced in at the target
 * XZ, overwriting terrain, at the flat surface in mining dimensions and at a clamped height
 * elsewhere, as vanilla does for nether portals. Refuses rather than destroy a container,
 * another portal, or an unbreakable block.
 */
final class ForcedPlacement implements PortalSearchStrategy {
    private static final int MIN_FORCED_Y = 70;
    private static final int CEILING_MARGIN = 9;

    @Override
    public Optional<BlockPos> search(ServerLevel level, BlockPos center, Axis axis) {
        BlockPos origin;
        if (ModDimensions.ALL_MINING_DIMENSIONS.contains(level.dimension())) {
            origin = center;
        } else {
            int lowest = Math.max(level.getMinBuildHeight() + 1, MIN_FORCED_Y);
            int highest = Math.min(level.getMaxBuildHeight(), level.getMinBuildHeight() + level.getLogicalHeight()) - 1 - CEILING_MARGIN;
            if (highest < lowest) return Optional.empty();
            origin = new BlockPos(center.getX(), Mth.clamp(center.getY(), lowest, highest), center.getZ());
        }

        return PortalFootprint.canForce(level, level.getWorldBorder(), origin, axis) ? Optional.of(origin) : Optional.empty();
    }
}
