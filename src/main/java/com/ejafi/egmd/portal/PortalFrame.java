package com.ejafi.egmd.portal;

import java.util.stream.Stream;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

record PortalFrame(BlockUtil.FoundRectangle interior, Axis axis) {
    static final int MIN_WIDTH = 2;
    static final int MAX_WIDTH = 21;
    static final int MIN_HEIGHT = 3;
    static final int MAX_HEIGHT = 21;

    static Direction resolveAlongDirection(Axis axis) {
        return axis == Axis.X ? Direction.EAST : Direction.SOUTH;
    }

    Direction getAlongDirection() {
        return resolveAlongDirection(axis);
    }

    Stream<BlockPos> streamInterior() {
        BlockPos minCorner = interior.minCorner;
        return BlockPos.betweenClosedStream(
                minCorner,
                minCorner.relative(getAlongDirection(), interior.axis1Size - 1).above(interior.axis2Size - 1));
    }
}
