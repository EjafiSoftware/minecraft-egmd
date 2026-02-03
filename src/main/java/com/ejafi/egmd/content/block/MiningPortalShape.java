package com.ejafi.egmd.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Handles the logic for detecting and creating the mining portal structure.
 * This class is responsible for verifying the frame and filling it with portal blocks.
 */
public class MiningPortalShape {
    public static final int MIN_WIDTH = 2;
    public static final int MAX_WIDTH = 21;
    public static final int MIN_HEIGHT = 3;
    public static final int MAX_HEIGHT = 21;

    private final LevelAccessor level;
    private final Axis axis;
    private final Direction rightDir;
    private int numPortalBlocks;
    private BlockPos bottomLeft;
    private int height;
    private final int width;

    private final Block portalBlock;
    private final Block frameBlock;

    public Block getPortalBlock() {
        return portalBlock;
    }

    public Block getFrameBlock() {
        return frameBlock;
    }

    public Axis getAxis() {
        return axis;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Creates a new portal shape detector.
     * 
     * @param level The level to check in
     * @param pos The position where ignition occurred
     * @param axis The axis of the portal (X or Z)
     * @param portalBlock The block to use for the portal interior
     * @param frameBlock The block that forms the frame
     */
    public MiningPortalShape(LevelAccessor level, BlockPos pos, Axis axis, Block portalBlock, Block frameBlock) {
        this.level = level;
        this.axis = axis;
        this.portalBlock = portalBlock;
        this.frameBlock = frameBlock;
        this.rightDir = axis == Axis.X ? Direction.WEST : Direction.SOUTH;
        this.bottomLeft = this.calculateBottomLeft(pos);
        if (this.bottomLeft == null) {
            this.bottomLeft = pos;
            this.width = 1;
            this.height = 1;
        } else {
            this.width = this.calculateWidth();
            if (this.width > 0) {
                this.height = this.calculateHeight();
            }
        }
    }

    private BlockPos calculateBottomLeft(BlockPos pos) {
        int minBuildHeight = this.level.getMinBuildHeight();
        for (int i = Math.max(minBuildHeight, pos.getY() - MAX_HEIGHT); pos.getY() > i && this.isEmpty(this.level.getBlockState(pos.below())); pos = pos.below()) {
        }

        Direction oppositeDirection = this.rightDir.getOpposite();
        int distance = this.getDistanceUntilEdge(pos, oppositeDirection) - 1;
        return distance < 0 ? null : pos.relative(oppositeDirection, distance);
    }

    private int calculateWidth() {
        int distance = this.getDistanceUntilEdge(this.bottomLeft, this.rightDir);
        return distance >= MIN_WIDTH && distance <= MAX_WIDTH ? distance : 0;
    }

    private int getDistanceUntilEdge(BlockPos pos, Direction direction) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int i = 0; i <= MAX_WIDTH; ++i) {
            mutablePos.set(pos).move(direction, i);
            BlockState state = this.level.getBlockState(mutablePos);
            if (!this.isEmpty(state)) {
                if (state.is(this.frameBlock)) {
                    return i;
                }
                break;
            }

            BlockState belowState = this.level.getBlockState(mutablePos.move(Direction.DOWN));
            if (!belowState.is(this.frameBlock)) {
                break;
            }
        }

        return 0;
    }

    private int calculateHeight() {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int distance = this.getDistanceUntilTop(mutablePos);
        return distance >= MIN_HEIGHT && distance <= MAX_HEIGHT && this.hasTopFrame(mutablePos, distance) ? distance : 0;
    }

    private boolean hasTopFrame(BlockPos.MutableBlockPos mutablePos, int currentHeight) {
        for (int i = 0; i < this.width; ++i) {
            mutablePos.set(this.bottomLeft).move(Direction.UP, currentHeight).move(this.rightDir, i);
            if (!this.level.getBlockState(mutablePos).is(this.frameBlock)) {
                return false;
            }
        }

        return true;
    }

    private int getDistanceUntilTop(BlockPos.MutableBlockPos mutablePos) {
        for (int y = 0; y < MAX_HEIGHT; ++y) {
            mutablePos.set(this.bottomLeft).move(Direction.UP, y).move(this.rightDir.getOpposite(), 1);
            if (!this.level.getBlockState(mutablePos).is(this.frameBlock)) {
                return y;
            }

            mutablePos.set(this.bottomLeft).move(Direction.UP, y).move(this.rightDir, this.width);
            if (!this.level.getBlockState(mutablePos).is(this.frameBlock)) {
                return y;
            }

            for (int x = 0; x < this.width; ++x) {
                mutablePos.set(this.bottomLeft).move(Direction.UP, y).move(this.rightDir, x);
                BlockState state = this.level.getBlockState(mutablePos);
                if (!this.isEmpty(state)) {
                    return y;
                }

                if (state.is(this.portalBlock)) {
                    ++this.numPortalBlocks;
                }
            }
        }

        return MAX_HEIGHT;
    }

    private boolean isEmpty(BlockState state) {
        return state.isAir() || state.is(this.portalBlock);
    }

    /**
     * @return true if the structure is a valid portal frame
     */
    public boolean isValid() {
        return this.bottomLeft != null && this.width >= MIN_WIDTH && this.width <= MAX_WIDTH && this.height >= MIN_HEIGHT && this.height <= MAX_HEIGHT;
    }

    /**
     * Fills the frame with portal blocks.
     */
    public void createPortalBlocks() {
        BlockState state = this.portalBlock.defaultBlockState().setValue(MiningPortalBlock.AXIS, this.axis);
        BlockPos.betweenClosed(this.bottomLeft, this.bottomLeft.relative(Direction.UP, this.height - 1).relative(this.rightDir, this.width - 1)).forEach((pos) -> {
            this.level.setBlock(pos, state, 18);
        });
    }

    /**
     * @return true if all interior blocks are portal blocks
     */
    public boolean isComplete() {
        return this.isValid() && this.numPortalBlocks == this.width * this.height;
    }
}