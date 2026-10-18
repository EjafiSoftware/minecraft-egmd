package com.ejafi.egmd.portal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import org.junit.jupiter.api.Test;

class PortalFootprintTest {
    @Test
    void locateCorners_placesCornersDiagonalToPortal_whenAxisIsX() {
        PortalFootprint alongX = new PortalFootprintFaker().withAxis(Axis.X).generate();
        BlockPos origin = alongX.origin();

        Map<PortalCorner, BlockPos> corners = alongX.locateCorners();

        assertThat(corners)
                .isEqualTo(Map.of(
                        PortalCorner.LOW_BOTTOM, origin.offset(-1, -1, 0),
                        PortalCorner.HIGH_BOTTOM, origin.offset(PortalFrame.MIN_WIDTH, -1, 0),
                        PortalCorner.LOW_TOP, origin.offset(-1, PortalFrame.MIN_HEIGHT, 0),
                        PortalCorner.HIGH_TOP, origin.offset(PortalFrame.MIN_WIDTH, PortalFrame.MIN_HEIGHT, 0)));
    }

    @Test
    void locateCorners_placesCornersDiagonalToPortal_whenAxisIsZ() {
        PortalFootprint alongZ = new PortalFootprintFaker().withAxis(Axis.Z).generate();
        BlockPos origin = alongZ.origin();

        Map<PortalCorner, BlockPos> corners = alongZ.locateCorners();

        assertThat(corners)
                .isEqualTo(Map.of(
                        PortalCorner.LOW_BOTTOM, origin.offset(0, -1, -1),
                        PortalCorner.HIGH_BOTTOM, origin.offset(0, -1, PortalFrame.MIN_WIDTH),
                        PortalCorner.LOW_TOP, origin.offset(0, PortalFrame.MIN_HEIGHT, -1),
                        PortalCorner.HIGH_TOP, origin.offset(0, PortalFrame.MIN_HEIGHT, PortalFrame.MIN_WIDTH)));
    }
}
