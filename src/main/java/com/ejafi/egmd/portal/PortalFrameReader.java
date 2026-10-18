package com.ejafi.egmd.portal;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;

final class PortalFrameReader {
    private final BlockGetter level;
    private final DimensionPortalBlock portalBlock;
    private final Block frameBlock;

    PortalFrameReader(BlockGetter level, DimensionPortalBlock portalBlock) {
        this.level = level;
        this.portalBlock = portalBlock;
        this.frameBlock = portalBlock.getFrameBlock();
    }

    static boolean isOpenCell(BlockState state) {
        return state.isAir() || state.getBlock() instanceof BaseFireBlock;
    }

    static boolean isFrame(BlockState state, Block frameBlock) {
        return resolveInfestedHost(state).equals(frameBlock);
    }

    static Block resolveInfestedHost(BlockState state) {
        return state.getBlock() instanceof InfestedBlock infested ? infested.getHostBlock() : state.getBlock();
    }

    Optional<PortalFrame> find(BlockPos pos, Axis axis) {
        Direction along = PortalFrame.resolveAlongDirection(axis);
        BlockPos floor = findFloor(pos);
        int spanBehind = measureSpanToFrame(floor, along.getOpposite());
        if (spanBehind == 0) {
            return Optional.empty();
        }

        BlockPos minCorner = floor.relative(along.getOpposite(), spanBehind - 1);
        int width = measureSpanToFrame(minCorner, along);
        if (width < PortalFrame.MIN_WIDTH || width > PortalFrame.MAX_WIDTH) {
            return Optional.empty();
        }

        int height = measureOpenHeight(new PortalFrame(new BlockUtil.FoundRectangle(minCorner, width, 1), axis));
        if (height < PortalFrame.MIN_HEIGHT) {
            return Optional.empty();
        }

        var frame = new PortalFrame(new BlockUtil.FoundRectangle(minCorner, width, height), axis);
        return hasTopFrame(frame) ? Optional.of(frame) : Optional.empty();
    }

    boolean isComplete(PortalFrame frame) {
        return countPortalBlocks(frame) == (long) frame.interior().axis1Size * frame.interior().axis2Size;
    }

    boolean isEmpty(PortalFrame frame) {
        return countPortalBlocks(frame) == 0;
    }

    Map<PortalCorner, BlockState> readCorners(PortalFrame frame) {
        return PortalCorner.locateAll(frame).entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey, entry -> level.getBlockState(entry.getValue())));
    }

    private BlockPos findFloor(BlockPos start) {
        int lowestY = Math.max(level.getMinBuildHeight(), start.getY() - PortalFrame.MAX_HEIGHT);
        BlockPos pos = start;
        while (pos.getY() > lowestY && isOpenOrPortal(level.getBlockState(pos.below()))) {
            pos = pos.below();
        }
        return pos;
    }

    private int measureSpanToFrame(BlockPos start, Direction direction) {
        var cell = new BlockPos.MutableBlockPos();
        for (int distance = 0; distance <= PortalFrame.MAX_WIDTH; ++distance) {
            cell.set(start).move(direction, distance);
            BlockState state = level.getBlockState(cell);
            if (!isOpenOrPortal(state)) {
                return isFrame(state, frameBlock) ? distance : 0;
            }

            if (!isFrame(level.getBlockState(cell.move(Direction.DOWN)), frameBlock)) {
                return 0;
            }
        }

        return 0;
    }

    private int measureOpenHeight(PortalFrame bottomRow) {
        Direction along = bottomRow.getAlongDirection();
        int width = bottomRow.interior().axis1Size;
        for (int y = 0; y < PortalFrame.MAX_HEIGHT; ++y) {
            BlockPos rowStart = bottomRow.interior().minCorner.above(y);
            if (!isFrame(level.getBlockState(rowStart.relative(along, -1)), frameBlock)
                    || !isFrame(level.getBlockState(rowStart.relative(along, width)), frameBlock)
                    || !isOpenRow(rowStart, bottomRow)) {
                return y;
            }
        }

        return PortalFrame.MAX_HEIGHT;
    }

    private boolean isOpenRow(BlockPos rowStart, PortalFrame bottomRow) {
        Direction along = bottomRow.getAlongDirection();
        for (int x = 0; x < bottomRow.interior().axis1Size; ++x) {
            if (!isOpenOrPortal(level.getBlockState(rowStart.relative(along, x)))) {
                return false;
            }
        }

        return true;
    }

    private boolean hasTopFrame(PortalFrame frame) {
        BlockPos topStart = frame.interior().minCorner.above(frame.interior().axis2Size);
        for (int x = 0; x < frame.interior().axis1Size; ++x) {
            if (!isFrame(level.getBlockState(topStart.relative(frame.getAlongDirection(), x)), frameBlock)) {
                return false;
            }
        }

        return true;
    }

    private long countPortalBlocks(PortalFrame frame) {
        return frame.streamInterior()
                .filter(pos -> level.getBlockState(pos).is(portalBlock))
                .count();
    }

    private boolean isOpenOrPortal(BlockState state) {
        return isOpenCell(state) || state.is(portalBlock);
    }
}
