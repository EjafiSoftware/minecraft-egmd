package com.ejafi.egmd.portal;

import static org.assertj.core.api.Assertions.assertThat;

import com.ejafi.egmd.BlockPosFaker;
import net.datafaker.Faker;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PortalExitTest {
    private static final Faker FAKER = new Faker();

    private final BlockPos minCorner = new BlockPosFaker().generate();
    private final int height = FAKER.number().numberBetween(PortalFrame.MIN_HEIGHT, PortalFrame.MAX_HEIGHT + 1);
    private final BlockUtil.FoundRectangle narrowestPortal =
            new BlockUtil.FoundRectangle(minCorner, PortalFrame.MIN_WIDTH, height);

    @ParameterizedTest(name = "{0} axis")
    @CsvSource({"X, 1.0, 1.5, 0", "Z, -0.5, 1.0, 90"})
    void computeFront_centersSpawnOnBlockInFrontOfPortal_whenPortalHasMinimumWidth(
            Axis axis, double offsetX, double offsetZ, float yRot) {
        PortalExit exit = PortalExit.computeFront(narrowestPortal, axis);

        assertThat(exit).isEqualTo(new PortalExit(resolveFromMinCorner(offsetX, offsetZ), yRot));
    }

    @ParameterizedTest(name = "{0} axis")
    @CsvSource({"X, 1.0, -0.5, 180", "Z, 1.5, 1.0, 270"})
    void computeBack_centersSpawnOnBlockBehindPortal_whenPortalHasMinimumWidth(
            Axis axis, double offsetX, double offsetZ, float yRot) {
        PortalExit exit = PortalExit.computeBack(narrowestPortal, axis);

        assertThat(exit).isEqualTo(new PortalExit(resolveFromMinCorner(offsetX, offsetZ), yRot));
    }

    @ParameterizedTest(name = "{0} axis")
    @CsvSource({"X, 1.0, 0.5, 0", "Z, 0.5, 1.0, 90"})
    void computeInside_centersSpawnOnPortalBottomRow_whenPortalHasMinimumWidth(
            Axis axis, double offsetX, double offsetZ, float yRot) {
        PortalExit exit = PortalExit.computeInside(narrowestPortal, axis);

        assertThat(exit).isEqualTo(new PortalExit(resolveFromMinCorner(offsetX, offsetZ), yRot));
    }

    @Test
    void computeFront_centersSpawnOnPortalWidth_whenPortalIsWiderThanMinimum() {
        int width = FAKER.number().numberBetween(PortalFrame.MIN_WIDTH + 1, PortalFrame.MAX_WIDTH + 1);

        PortalExit exit = PortalExit.computeFront(new BlockUtil.FoundRectangle(minCorner, width, height), Axis.X);

        assertThat(exit).isEqualTo(new PortalExit(resolveFromMinCorner(width / 2.0, 1.5), 0.0F));
    }

    private Vec3 resolveFromMinCorner(double offsetX, double offsetZ) {
        return Vec3.atLowerCornerOf(minCorner).add(offsetX, 0.0, offsetZ);
    }
}
