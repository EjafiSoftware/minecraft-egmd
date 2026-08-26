package com.ejafi.egmd.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;

/**
 * Where an entity arrives when exiting a portal: centered on the portal's width, one and a
 * half blocks in front of it, facing away.
 *
 * @param position the spawn position
 * @param yRot     the spawn rotation around the Y axis, in degrees
 */
record PortalExitPlacement(Vec3 position, float yRot) {

    static PortalExitPlacement compute(BlockPos minCorner, Axis axis, int width) {
        Direction alongDir = axis == Axis.X ? Direction.EAST : Direction.SOUTH;
        Direction frontDir = alongDir.getClockWise();

        Vec3 bottomCenter = Vec3.atLowerCornerOf(minCorner)
                .add(Vec3.atLowerCornerOf(alongDir.getNormal()).scale(width / 2.0));
        Vec3 position = bottomCenter.add(Vec3.atLowerCornerOf(frontDir.getNormal()).scale(1.5));

        return new PortalExitPlacement(position, frontDir.toYRot());
    }
}
