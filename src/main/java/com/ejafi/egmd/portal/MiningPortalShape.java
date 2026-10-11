package com.ejafi.egmd.portal;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

final class MiningPortalShape {
    static final int MIN_WIDTH = 2;
    static final int MAX_WIDTH = 21;
    static final int MIN_HEIGHT = 3;
    static final int MAX_HEIGHT = 21;
    static final int PORTAL_UPDATE_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    private final BlockGetter level;
    private final Axis axis;
    private final Direction rightDir;
    private final MiningPortalBlock portalBlock;
    private final Block frameBlock;
    private final BlockPos bottomLeft;
    private final int width;
    private final int height;
    private final int portalBlockCount;

    MiningPortalShape(BlockGetter level, BlockPos pos, Axis axis, MiningPortalBlock portalBlock) {
        this.level = level;
        this.axis = axis;
        this.portalBlock = portalBlock;
        frameBlock = portalBlock.getFrameBlock();
        rightDir = axis == Axis.X ? Direction.WEST : Direction.SOUTH;
        BlockPos foundBottomLeft = calculateBottomLeft(pos);
        bottomLeft = foundBottomLeft == null ? pos : foundBottomLeft;
        width = foundBottomLeft == null ? 0 : calculateWidth();
        height = width == 0 ? 0 : calculateHeight();
        portalBlockCount = isValid() ? countPortalBlocks() : 0;
    }

    private @Nullable BlockPos calculateBottomLeft(BlockPos pos) {
        int lowestY = Math.max(level.getMinBuildHeight(), pos.getY() - MAX_HEIGHT);
        while (pos.getY() > lowestY && isEmpty(level.getBlockState(pos.below()))) {
            pos = pos.below();
        }

        Direction oppositeDirection = rightDir.getOpposite();
        int distance = getDistanceUntilEdge(pos, oppositeDirection) - 1;
        return distance < 0 ? null : pos.relative(oppositeDirection, distance);
    }

    private int calculateWidth() {
        int distance = getDistanceUntilEdge(bottomLeft, rightDir);
        return distance >= MIN_WIDTH && distance <= MAX_WIDTH ? distance : 0;
    }

    private int getDistanceUntilEdge(BlockPos pos, Direction direction) {
        var mutablePos = new BlockPos.MutableBlockPos();

        for (int i = 0; i <= MAX_WIDTH; ++i) {
            mutablePos.set(pos).move(direction, i);
            BlockState state = level.getBlockState(mutablePos);
            if (!isEmpty(state)) {
                if (isFrame(state, frameBlock)) {
                    return i;
                }
                break;
            }

            BlockState belowState = level.getBlockState(mutablePos.move(Direction.DOWN));
            if (!isFrame(belowState, frameBlock)) {
                break;
            }
        }

        return 0;
    }

    private int calculateHeight() {
        var mutablePos = new BlockPos.MutableBlockPos();
        int distance = getDistanceUntilTop(mutablePos);
        return distance >= MIN_HEIGHT && distance <= MAX_HEIGHT && hasTopFrame(mutablePos, distance) ? distance : 0;
    }

    private boolean hasTopFrame(BlockPos.MutableBlockPos mutablePos, int currentHeight) {
        for (int i = 0; i < width; ++i) {
            mutablePos.set(bottomLeft).move(Direction.UP, currentHeight).move(rightDir, i);
            if (!isFrame(level.getBlockState(mutablePos), frameBlock)) {
                return false;
            }
        }

        return true;
    }

    private int getDistanceUntilTop(BlockPos.MutableBlockPos mutablePos) {
        for (int y = 0; y < MAX_HEIGHT; ++y) {
            mutablePos.set(bottomLeft).move(Direction.UP, y).move(rightDir.getOpposite(), 1);
            if (!isFrame(level.getBlockState(mutablePos), frameBlock)) {
                return y;
            }

            mutablePos.set(bottomLeft).move(Direction.UP, y).move(rightDir, width);
            if (!isFrame(level.getBlockState(mutablePos), frameBlock)) {
                return y;
            }

            for (int x = 0; x < width; ++x) {
                mutablePos.set(bottomLeft).move(Direction.UP, y).move(rightDir, x);
                if (!isEmpty(level.getBlockState(mutablePos))) {
                    return y;
                }
            }
        }

        return MAX_HEIGHT;
    }

    private int countPortalBlocks() {
        return (int) streamInterior()
                .filter(pos -> level.getBlockState(pos).is(portalBlock))
                .count();
    }

    private Stream<BlockPos> streamInterior() {
        return BlockPos.betweenClosedStream(
                bottomLeft, bottomLeft.relative(Direction.UP, height - 1).relative(rightDir, width - 1));
    }

    private boolean isEmpty(BlockState state) {
        return isOpenCell(state) || state.is(portalBlock);
    }

    static boolean isOpenCell(BlockState state) {
        return state.isAir() || state.getBlock() instanceof BaseFireBlock;
    }

    boolean isValid() {
        return width != 0 && height != 0;
    }

    boolean isEmptyFrame() {
        return isValid() && portalBlockCount == 0;
    }

    static boolean isFrame(BlockState state, Block frameBlock) {
        return resolveInfestedHost(state).equals(frameBlock);
    }

    static Block resolveInfestedHost(BlockState state) {
        return state.getBlock() instanceof InfestedBlock infested ? infested.getHostBlock() : state.getBlock();
    }

    boolean isComplete() {
        return isValid() && portalBlockCount == width * height;
    }

    Map<PortalCorner, BlockState> readCorners() {
        if (!isValid()) {
            return Map.of();
        }

        BlockPos lowest = BlockPos.min(bottomLeft, bottomLeft.relative(rightDir, width - 1));
        return PortalCorner.locateAll(new BlockUtil.FoundRectangle(lowest, width, height), axis).entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey, entry -> level.getBlockState(entry.getValue())));
    }

    void createPortalBlocks(LevelWriter writer) {
        BlockState state = portalBlock.defaultBlockState().setValue(MiningPortalBlock.AXIS, axis);
        streamInterior().forEach(pos -> writer.setBlock(pos, state, PORTAL_UPDATE_FLAGS));
    }
}
