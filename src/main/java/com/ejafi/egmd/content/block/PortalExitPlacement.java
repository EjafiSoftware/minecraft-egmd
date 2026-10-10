package com.ejafi.egmd.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;

/**
 * Where an entity arrives when exiting a portal: centered on the portal's width, on the block in
 * front of it, behind it, or inside it, facing away from the portal.
 *
 * @param position the spawn position
 * @param yRot     the spawn rotation around the Y axis, in degrees
 */
record PortalExitPlacement(Vec3 position, float yRot) {

    /**
     * The block directly in front of the portal, on the side its generated landing platform covers.
     */
    static PortalExitPlacement computeFront(BlockPos minCorner, Axis axis, int width) {
        Direction frontDir = resolveAlongDirection(axis).getClockWise();
        return new PortalExitPlacement(computeOffsetFromCenter(minCorner, axis, width, frontDir, 1.0), frontDir.toYRot());
    }

    /**
     * The block directly behind the portal.
     */
    static PortalExitPlacement computeBack(BlockPos minCorner, Axis axis, int width) {
        Direction backDir = resolveAlongDirection(axis).getCounterClockWise();
        return new PortalExitPlacement(computeOffsetFromCenter(minCorner, axis, width, backDir, 1.0), backDir.toYRot());
    }

    /**
     * The bottom row of the portal itself, which is always clear of blocks.
     */
    static PortalExitPlacement computeInside(BlockPos minCorner, Axis axis, int width) {
        Direction frontDir = resolveAlongDirection(axis).getClockWise();
        return new PortalExitPlacement(computeOffsetFromCenter(minCorner, axis, width, frontDir, 0.0), frontDir.toYRot());
    }

    private static Direction resolveAlongDirection(Axis axis) {
        return axis == Axis.X ? Direction.EAST : Direction.SOUTH;
    }

    private static Vec3 computeOffsetFromCenter(BlockPos minCorner, Axis axis, int width, Direction side, double distance) {
        Vec3 along = Vec3.atLowerCornerOf(resolveAlongDirection(axis).getNormal()).scale(width / 2.0 - 0.5);
        Vec3 outward = Vec3.atLowerCornerOf(side.getNormal()).scale(distance);
        return Vec3.atBottomCenterOf(minCorner).add(along).add(outward);
    }
}
