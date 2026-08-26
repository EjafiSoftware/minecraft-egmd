package com.ejafi.egmd.content.block;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class PortalExitPlacementTest {

    @Test
    void compute_centersSpawnInFrontOfPortal_whenAxisIsX() {
        PortalExitPlacement placement = PortalExitPlacement.compute(new BlockPos(10, 64, 10), Axis.X, 2);

        assertEquals(new Vec3(11.0, 64.0, 11.5), placement.position());
        assertEquals(0.0F, placement.yRot());
    }

    @Test
    void compute_centersSpawnInFrontOfPortal_whenAxisIsZ() {
        PortalExitPlacement placement = PortalExitPlacement.compute(new BlockPos(10, 64, 10), Axis.Z, 2);

        assertEquals(new Vec3(8.5, 64.0, 11.0), placement.position());
        assertEquals(90.0F, placement.yRot());
    }
}
