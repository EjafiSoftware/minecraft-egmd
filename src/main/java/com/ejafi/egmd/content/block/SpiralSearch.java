package com.ejafi.egmd.content.block;

import com.ejafi.egmd.core.registration.ModDimensions;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Spirals outward from the target XZ and returns the nearest column where a portal fits without
 * replacing anything but ground. In mining dimensions every column is tried at the flat surface Y,
 * so a portal never sinks into a mined-out pit; elsewhere each column is tried at its surface.
 */
final class SpiralSearch implements PortalSearchStrategy {
    static final int RADIUS = 16;

    @Override
    public Optional<BlockPos> search(ServerLevel level, BlockPos center, Axis axis) {
        WorldBorder border = level.getWorldBorder();
        boolean miningDimension = ModDimensions.ALL_MINING_DIMENSIONS.contains(level.dimension());

        for (BlockPos.MutableBlockPos candidate : BlockPos.spiralAround(center, RADIUS, Direction.EAST, Direction.SOUTH)) {
            if (!miningDimension) {
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, candidate.getX(), candidate.getZ());
                if (surfaceY <= level.getMinBuildHeight()) continue;
                candidate.setY(surfaceY);
            }

            if (PortalFootprint.canHost(level, border, candidate, axis)) {
                return Optional.of(candidate.immutable());
            }
        }
        return Optional.empty();
    }
}
