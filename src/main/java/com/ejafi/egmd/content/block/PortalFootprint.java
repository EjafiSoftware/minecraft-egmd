package com.ejafi.egmd.content.block;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;

/**
 * The blocks a generated portal occupies, as built by {@link PortalStructureBuilder}: the frame
 * plane, the landing platform beneath it, and the exit space in front of it where travelers arrive.
 *
 * <p>Positions are measured from the origin, the bottom portal block with the lowest coordinate
 * along the portal's axis.</p>
 */
final class PortalFootprint {
    static final int EXIT_HEADROOM = 2;

    private PortalFootprint() {}

    /**
     * Returns {@code true} if a portal can be built at {@code origin} without replacing anything but
     * the ground under its platform: the frame plane and exit space are open, and nothing under the
     * platform is a container, a portal, or unbreakable.
     */
    static boolean canHost(BlockGetter level, WorldBorder border, BlockPos origin, Axis axis) {
        return fitsInWorld(level, border, origin, axis)
                && Stream.concat(listPlaneCells(origin, axis).stream(), listExitCells(origin, axis).stream())
                        .allMatch(pos -> isOpen(level.getBlockState(pos)))
                && listPlatformCells(origin, axis).stream().allMatch(pos -> isOverwritable(level, pos));
    }

    /**
     * Returns {@code true} if a portal can be forced into {@code origin}, overwriting terrain, without
     * destroying a container, another portal, or an unbreakable block.
     */
    static boolean canForce(BlockGetter level, WorldBorder border, BlockPos origin, Axis axis) {
        return fitsInWorld(level, border, origin, axis)
                && Stream.of(listPlaneCells(origin, axis), listExitCells(origin, axis), listPlatformCells(origin, axis))
                        .flatMap(List::stream)
                        .allMatch(pos -> isOverwritable(level, pos));
    }

    /**
     * The exit space in front of the portal, which a forced placement clears.
     */
    static List<BlockPos> listExitCells(BlockPos origin, Axis axis) {
        List<BlockPos> cells = new ArrayList<>();
        for (int along = 0; along < MiningPortalShape.MIN_WIDTH; along++) {
            for (int up = 0; up < EXIT_HEADROOM; up++) {
                cells.add(resolveCell(origin, axis, along, up, 1));
            }
        }
        return cells;
    }

    private static List<BlockPos> listPlaneCells(BlockPos origin, Axis axis) {
        List<BlockPos> cells = new ArrayList<>();
        for (int along = -1; along <= MiningPortalShape.MIN_WIDTH; along++) {
            for (int up = 0; up <= MiningPortalShape.MIN_HEIGHT; up++) {
                cells.add(resolveCell(origin, axis, along, up, 0));
            }
        }
        return cells;
    }

    private static List<BlockPos> listPlatformCells(BlockPos origin, Axis axis) {
        List<BlockPos> cells = new ArrayList<>();
        for (int along = -1; along <= MiningPortalShape.MIN_WIDTH; along++) {
            for (int out = -1; out <= MiningPortalShape.PLATFORM_DEPTH - 2; out++) {
                cells.add(resolveCell(origin, axis, along, -1, out));
            }
        }
        return cells;
    }

    private static boolean fitsInWorld(BlockGetter level, WorldBorder border, BlockPos origin, Axis axis) {
        return origin.getY() - 1 > level.getMinBuildHeight()
                && origin.getY() + MiningPortalShape.MIN_HEIGHT < level.getMaxBuildHeight()
                && border.isWithinBounds(resolveCell(origin, axis, -1, 0, -1))
                && border.isWithinBounds(resolveCell(origin, axis, MiningPortalShape.MIN_WIDTH, 0, MiningPortalShape.PLATFORM_DEPTH - 2));
    }

    private static boolean isOpen(BlockState state) {
        return (state.isAir() || state.canBeReplaced()) && state.getFluidState().isEmpty() && !state.hasBlockEntity();
    }

    private static boolean isOverwritable(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.hasBlockEntity() && !(state.getBlock() instanceof Portal) && state.getDestroySpeed(level, pos) >= 0;
    }

    private static BlockPos resolveCell(BlockPos origin, Axis axis, int along, int up, int out) {
        Direction alongDir = axis == Axis.X ? Direction.EAST : Direction.SOUTH;
        return origin.relative(alongDir, along).relative(alongDir.getClockWise(), out).above(up);
    }
}
