package com.ejafi.egmd.portal;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

record PortalFootprint(BlockPos origin, Axis axis) {
    private static final int PLATFORM_DEPTH = 3;
    private static final int EXIT_HEADROOM = 2;
    private static final BoundingBox PLANE =
            new BoundingBox(-1, 0, 0, PortalFrame.MIN_WIDTH, PortalFrame.MIN_HEIGHT, 0);
    private static final BoundingBox PORTAL =
            new BoundingBox(0, 0, 0, PortalFrame.MIN_WIDTH - 1, PortalFrame.MIN_HEIGHT - 1, 0);
    private static final BoundingBox EXIT = new BoundingBox(0, 0, 1, PortalFrame.MIN_WIDTH - 1, EXIT_HEADROOM - 1, 1);
    private static final BoundingBox PLATFORM =
            new BoundingBox(-1, -1, -1, PortalFrame.MIN_WIDTH, -1, PLATFORM_DEPTH - 2);

    PortalFootprint {
        origin = origin.immutable();
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

    PortalFrame locateFrame() {
        return new PortalFrame(
                new BlockUtil.FoundRectangle(origin, PortalFrame.MIN_WIDTH, PortalFrame.MIN_HEIGHT), axis);
    }

    List<BlockPos> listExitCells() {
        return listCells(EXIT);
    }

    List<BlockPos> listFrameCells() {
        return BlockPos.betweenClosedStream(PLANE)
                .filter(offset -> !PORTAL.isInside(offset))
                .map(this::resolveCell)
                .toList();
    }

    List<BlockPos> listPlatformCells() {
        return listCells(PLATFORM);
    }

    Map<PortalCorner, BlockPos> locateCorners() {
        return PortalCorner.locateAll(locateFrame());
    }

    private List<BlockPos> listPlaneCells() {
        return listCells(PLANE);
    }

    private List<BlockPos> listCells(BoundingBox offsets) {
        return BlockPos.betweenClosedStream(offsets).map(this::resolveCell).toList();
    }

    private BlockPos resolveCell(Vec3i offset) {
        Direction along = PortalFrame.resolveAlongDirection(axis);
        return origin.relative(along, offset.getX())
                .relative(along.getClockWise(), offset.getZ())
                .above(offset.getY());
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
}
