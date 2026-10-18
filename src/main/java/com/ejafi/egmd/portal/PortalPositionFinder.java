package com.ejafi.egmd.portal;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;

final class PortalPositionFinder {
    private static final int SPIRAL_RADIUS = 16;
    private static final int MIN_FORCED_Y = 70;
    private static final int CEILING_MARGIN = 9;

    private PortalPositionFinder() {}

    static Optional<PortalSite> find(LevelReader level, ArrivalTarget target) {
        PortalFootprint footprint = target.footprint();
        var center =
                new PortalFootprint(clampInsideBorder(level.getWorldBorder(), footprint.origin()), footprint.axis());
        loadChunks(level, center.origin());

        return switch (target) {
            case ArrivalTarget.OnFlatSurface onFlatSurface -> findOnFlatSurface(level, center);
            case ArrivalTarget.OnTerrain onTerrain -> findOnTerrain(level, center);
        };
    }

    private static Optional<PortalSite> findOnFlatSurface(LevelReader level, PortalFootprint center) {
        return searchSpiralAtFixedY(level, center)
                .<PortalSite>map(PortalSite.Open::new)
                .or(() -> force(level, center));
    }

    private static Optional<PortalSite> findOnTerrain(LevelReader level, PortalFootprint center) {
        return searchSpiralOnSurface(level, center)
                .<PortalSite>map(PortalSite.Open::new)
                .or(() ->
                        resolveForcedFootprint(level.dimensionType(), center).flatMap(forced -> force(level, forced)));
    }

    private static Optional<PortalSite> force(CollisionGetter level, PortalFootprint footprint) {
        return canForce(level, footprint) ? Optional.of(new PortalSite.Forced(footprint)) : Optional.empty();
    }

    static boolean canHost(CollisionGetter level, PortalFootprint footprint) {
        return fitsInWorld(level, footprint)
                && Stream.concat(footprint.listPlaneCells().stream(), footprint.listExitCells().stream())
                        .allMatch(pos -> isOpen(level.getBlockState(pos)))
                && footprint.listPlatformCells().stream().allMatch(pos -> isOverwritable(level, pos));
    }

    static boolean canForce(CollisionGetter level, PortalFootprint footprint) {
        return fitsInWorld(level, footprint)
                && Stream.of(footprint.listPlaneCells(), footprint.listExitCells(), footprint.listPlatformCells())
                        .flatMap(List::stream)
                        .allMatch(pos -> isOverwritable(level, pos));
    }

    private static boolean fitsInWorld(CollisionGetter level, PortalFootprint footprint) {
        return footprint.origin().getY() - 1 > level.getMinBuildHeight()
                && footprint.origin().getY() + PortalFrame.MIN_HEIGHT < level.getMaxBuildHeight()
                && footprint.listPlatformCells().stream().allMatch(level.getWorldBorder()::isWithinBounds);
    }

    private static boolean isOpen(BlockState state) {
        return (state.isAir() || state.canBeReplaced()) && state.getFluidState().isEmpty() && !state.hasBlockEntity();
    }

    private static boolean isOverwritable(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.hasBlockEntity()
                && !(state.getBlock() instanceof Portal)
                && state.getDestroySpeed(level, pos) >= 0;
    }

    static Optional<PortalFootprint> searchSpiralAtFixedY(CollisionGetter level, PortalFootprint center) {
        return findFirstHost(level, streamSpiral(center));
    }

    static Optional<PortalFootprint> searchSpiralOnSurface(LevelReader level, PortalFootprint center) {
        return findFirstHost(level, streamSpiral(center).map(column -> moveToSurface(level, column)));
    }

    private static Stream<PortalFootprint> streamSpiral(PortalFootprint center) {
        return StreamSupport.stream(
                        BlockPos.spiralAround(center.origin(), SPIRAL_RADIUS, Direction.EAST, Direction.SOUTH)
                                .spliterator(),
                        false)
                .map(column -> new PortalFootprint(column, center.axis()));
    }

    private static PortalFootprint moveToSurface(LevelReader level, PortalFootprint column) {
        BlockPos origin = column.origin();
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, origin.getX(), origin.getZ());
        return new PortalFootprint(origin.atY(surfaceY), column.axis());
    }

    private static Optional<PortalFootprint> findFirstHost(CollisionGetter level, Stream<PortalFootprint> candidates) {
        return candidates.filter(footprint -> canHost(level, footprint)).findFirst();
    }

    static Optional<PortalFootprint> resolveForcedFootprint(DimensionType type, PortalFootprint center) {
        int lowest = Math.max(type.minY() + 1, MIN_FORCED_Y);
        int highest = Math.min(type.minY() + type.height(), type.minY() + type.logicalHeight()) - 1 - CEILING_MARGIN;
        if (highest < lowest) {
            return Optional.empty();
        }
        BlockPos origin = center.origin();
        return Optional.of(new PortalFootprint(origin.atY(Mth.clamp(origin.getY(), lowest, highest)), center.axis()));
    }

    static BlockPos clampInsideBorder(WorldBorder border, BlockPos pos) {
        int minX = Mth.ceil(border.getMinX()) + 1;
        int maxX = Mth.floor(border.getMaxX()) - PortalFrame.MIN_WIDTH - 1;
        int minZ = Mth.ceil(border.getMinZ()) + 1;
        int maxZ = Mth.floor(border.getMaxZ()) - PortalFrame.MIN_WIDTH - 1;
        if (minX > maxX || minZ > maxZ) {
            return pos;
        }
        return new BlockPos(Mth.clamp(pos.getX(), minX, maxX), pos.getY(), Mth.clamp(pos.getZ(), minZ, maxZ));
    }

    private static void loadChunks(LevelReader level, BlockPos center) {
        int reach = SPIRAL_RADIUS + PortalFrame.MIN_WIDTH;
        for (int x = (center.getX() - reach) >> 4; x <= (center.getX() + reach) >> 4; x++) {
            for (int z = (center.getZ() - reach) >> 4; z <= (center.getZ() + reach) >> 4; z++) {
                level.getChunk(x, z);
            }
        }
    }
}
