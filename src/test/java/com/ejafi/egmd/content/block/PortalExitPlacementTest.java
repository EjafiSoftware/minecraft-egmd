package com.ejafi.egmd.content.block;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PortalExitPlacementTest {
    private static final BlockPos MIN_CORNER = new BlockPos(10, 64, 10);
    private static final int WIDTH = 2;

    @ParameterizedTest(name = "{0} axis, {1} side")
    @CsvSource({
            "X, FRONT,  11.0, 11.5,   0",
            "Z, FRONT,   9.5, 11.0,  90",
            "X, BACK,   11.0,  9.5, 180",
            "Z, BACK,   11.5, 11.0, 270",
            "X, INSIDE, 11.0, 10.5,   0",
            "Z, INSIDE, 10.5, 11.0,  90"
    })
    void compute_centersSpawnOnBlockBesidePortal_whenGivenEachAxisAndSide(Axis axis, Side side, double x, double z, float yRot) {
        PortalExitPlacement placement = side.compute(axis);

        assertEquals(new PortalExitPlacement(new Vec3(x, 64.0, z), yRot), placement);
    }

    @Test
    void computeFront_centersSpawnOnPortalWidth_whenPortalIsWiderThanMinimum() {
        PortalExitPlacement placement = PortalExitPlacement.computeFront(MIN_CORNER, Axis.X, 5);

        assertEquals(new PortalExitPlacement(new Vec3(12.5, 64.0, 11.5), 0.0F), placement);
    }

    /**
     * The three exit sides, so one parameterized test can cover each placement method.
     */
    enum Side {
        FRONT, BACK, INSIDE;

        PortalExitPlacement compute(Axis axis) {
            return switch (this) {
                case FRONT -> PortalExitPlacement.computeFront(MIN_CORNER, axis, WIDTH);
                case BACK -> PortalExitPlacement.computeBack(MIN_CORNER, axis, WIDTH);
                case INSIDE -> PortalExitPlacement.computeInside(MIN_CORNER, axis, WIDTH);
            };
        }
    }
}
