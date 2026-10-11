package com.ejafi.egmd.portal;

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

    PortalFrameBuilder(LevelWriter level, Block frameBlock) {
        this.level = level;
        this.frame = frameBlock.defaultBlockState();
    }

    void build(BlockUtil.FoundRectangle interior, Axis axis) {
        Direction along = PortalFootprint.resolveAlongDirection(axis);
        BlockPos corner = interior.minCorner;

        for (int offset = -1; offset <= interior.axis1Size; offset++) {
            placeFrame(corner.relative(along, offset).below());
            placeFrame(corner.relative(along, offset).above(interior.axis2Size));
        }

        for (int up = 0; up < interior.axis2Size; up++) {
            placeFrame(corner.relative(along, -1).above(up));
            placeFrame(corner.relative(along, interior.axis1Size).above(up));
        }
    }

    private void placeFrame(BlockPos pos) {
        level.setBlock(pos, frame, Block.UPDATE_ALL);
    }
}
