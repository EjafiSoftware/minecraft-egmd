package com.ejafi.egmd.portal;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

record PortalExit(Vec3 position, float yRot) {

    static PortalExit computeFront(PortalFrame frame) {
        return stepOutside(computeCenter(frame), resolveFrontDirection(frame));
    }

    static PortalExit computeBack(PortalFrame frame) {
        return stepOutside(computeCenter(frame), resolveFrontDirection(frame).getOpposite());
    }

    static PortalExit computeInside(PortalFrame frame) {
        return new PortalExit(computeCenter(frame), resolveFrontDirection(frame).toYRot());
    }

    private static PortalExit stepOutside(Vec3 center, Direction side) {
        return new PortalExit(center.relative(side, 1.0), side.toYRot());
    }

    private static Direction resolveFrontDirection(PortalFrame frame) {
        return frame.getAlongDirection().getClockWise();
    }

    private static Vec3 computeCenter(PortalFrame frame) {
        return Vec3.atBottomCenterOf(frame.interior().minCorner)
                .relative(frame.getAlongDirection(), frame.interior().axis1Size / 2.0 - 0.5);
    }
}
