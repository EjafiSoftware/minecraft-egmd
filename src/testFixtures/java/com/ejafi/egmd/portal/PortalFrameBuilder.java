package com.ejafi.egmd.portal;

import static com.ejafi.egmd.portal.PortalCornersFaker.fillCorners;

import java.util.Map;
import java.util.Objects;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

final class PortalFrameBuilder {
    private final LevelWriter level;
    private final BlockState frame;
    private Map<PortalCorner, BlockState> corners;

    PortalFrameBuilder(LevelWriter level, Block frameBlock) {
        this.level = level;
        this.frame = frameBlock.defaultBlockState();
        this.corners = fillCorners(frame);
    }

    PortalFrameBuilder withCornerBlock(Block cornerBlock) {
        this.corners = fillCorners(cornerBlock.defaultBlockState());
        return this;
    }

    PortalFrameBuilder withCorners(Map<PortalCorner, BlockState> corners) {
        this.corners = Map.copyOf(corners);
        return this;
    }

    void build(BlockUtil.FoundRectangle interior, Axis axis) {
        Direction along = PortalFootprint.resolveAlongDirection(axis);
        BlockPos bottomLeft = interior.minCorner;

        for (int offset = 0; offset < interior.axis1Size; offset++) {
            place(bottomLeft.relative(along, offset).below(), frame);
            place(bottomLeft.relative(along, offset).above(interior.axis2Size), frame);
        }

        for (int up = 0; up < interior.axis2Size; up++) {
            place(bottomLeft.relative(along, -1).above(up), frame);
            place(bottomLeft.relative(along, interior.axis1Size).above(up), frame);
        }

        Map<PortalCorner, BlockPos> cornerCells = PortalCorner.locateAll(interior, axis);
        corners.forEach((corner, state) -> place(Objects.requireNonNull(cornerCells.get(corner)), state));
    }

    private void place(BlockPos pos, BlockState state) {
        level.setBlock(pos, state, Block.UPDATE_ALL);
    }
}
