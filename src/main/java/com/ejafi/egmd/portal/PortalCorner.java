package com.ejafi.egmd.portal;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

enum PortalCorner {
    LOW_BOTTOM,
    HIGH_BOTTOM,
    LOW_TOP,
    HIGH_TOP;

    static <T> Map<PortalCorner, T> mapAll(Function<PortalCorner, T> mapper) {
        return Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(Function.identity(), mapper));
    }

    static Map<PortalCorner, BlockPos> locateAll(BlockUtil.FoundRectangle interior, Axis axis) {
        return mapAll(corner -> corner.locate(interior, axis));
    }

    boolean isBottom() {
        return switch (this) {
            case LOW_BOTTOM, HIGH_BOTTOM -> true;
            case LOW_TOP, HIGH_TOP -> false;
        };
    }

    private BlockPos locate(BlockUtil.FoundRectangle interior, Axis axis) {
        Direction along = PortalFootprint.resolveAlongDirection(axis);
        BlockPos low = interior.minCorner.relative(along, -1);
        BlockPos high = interior.minCorner.relative(along, interior.axis1Size);
        return switch (this) {
            case LOW_BOTTOM -> low.below();
            case HIGH_BOTTOM -> high.below();
            case LOW_TOP -> low.above(interior.axis2Size);
            case HIGH_TOP -> high.above(interior.axis2Size);
        };
    }
}
