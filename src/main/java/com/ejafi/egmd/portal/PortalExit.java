package com.ejafi.egmd.portal;

import net.minecraft.BlockUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;

record PortalExit(Vec3 position, float yRot) {

    static PortalExit computeFront(BlockUtil.FoundRectangle rectangle, Axis axis) {
        return stepOutside(computeCenter(rectangle, axis), resolveFrontDirection(axis));
    }

    static PortalExit computeBack(BlockUtil.FoundRectangle rectangle, Axis axis) {
        return stepOutside(
                computeCenter(rectangle, axis), resolveFrontDirection(axis).getOpposite());
    }

    static PortalExit computeInside(BlockUtil.FoundRectangle rectangle, Axis axis) {
        return new PortalExit(
                computeCenter(rectangle, axis), resolveFrontDirection(axis).toYRot());
    }

    private static PortalExit stepOutside(Vec3 center, Direction side) {
        return new PortalExit(center.relative(side, 1.0), side.toYRot());
    }

    private static Direction resolveFrontDirection(Axis axis) {
        return PortalFootprint.resolveAlongDirection(axis).getClockWise();
    }

    private static Vec3 computeCenter(BlockUtil.FoundRectangle rectangle, Axis axis) {
        return Vec3.atBottomCenterOf(rectangle.minCorner)
                .relative(PortalFootprint.resolveAlongDirection(axis), rectangle.axis1Size / 2.0 - 0.5);
    }
}
