package com.ejafi.egmd.dimension;

import static org.assertj.core.api.Assertions.assertThat;

import net.datafaker.Faker;
import net.minecraft.world.level.dimension.DimensionType;
import org.junit.jupiter.api.Test;

class SlimeSpawnCheckerTest {
    private static final Faker FAKER = new Faker();
    private static final int MAX_LIGHT = 15;

    @Test
    void isInSurfaceBand_acceptsY_whenYIsInSurfaceBand() {
        int y = FAKER.number().numberBetween(SlimeSpawnChecker.SURFACE_MIN_Y, SlimeSpawnChecker.SURFACE_MAX_Y + 1);

        boolean inBand = SlimeSpawnChecker.isInSurfaceBand(y);

        assertThat(inBand).isTrue();
    }

    @Test
    void isInSurfaceBand_rejectsY_whenYIsBelowSurfaceBand() {
        int y = FAKER.number().numberBetween(DimensionType.MIN_Y, SlimeSpawnChecker.SURFACE_MIN_Y);

        boolean inBand = SlimeSpawnChecker.isInSurfaceBand(y);

        assertThat(inBand).isFalse();
    }

    @Test
    void isInSurfaceBand_rejectsY_whenYIsAboveSurfaceBand() {
        int y = FAKER.number().numberBetween(SlimeSpawnChecker.SURFACE_MAX_Y + 1, DimensionType.MAX_Y);

        boolean inBand = SlimeSpawnChecker.isInSurfaceBand(y);

        assertThat(inBand).isFalse();
    }

    @Test
    void passesRoll_acceptsSpawn_whenLightIsWithinDarknessRoll() {
        SlimeSpawnChecker.SurfaceRoll roll = generatePassingRoll();

        boolean passed = SlimeSpawnChecker.passesRoll(roll, FAKER.number().numberBetween(0, roll.darkness() + 1));

        assertThat(passed).isTrue();
    }

    @Test
    void passesRoll_refusesSpawn_whenChanceRollFails() {
        SlimeSpawnChecker.SurfaceRoll roll = new SlimeSpawnChecker.SurfaceRoll(
                (float) FAKER.random().nextDouble(SlimeSpawnChecker.SPAWN_CHANCE, 1),
                FAKER.number().numberBetween(0, SlimeSpawnChecker.DARKNESS_BOUND));

        boolean passed = SlimeSpawnChecker.passesRoll(roll, 0);

        assertThat(passed).isFalse();
    }

    @Test
    void passesRoll_refusesSpawn_whenLightExceedsDarknessRoll() {
        SlimeSpawnChecker.SurfaceRoll roll = generatePassingRoll();

        boolean passed =
                SlimeSpawnChecker.passesRoll(roll, FAKER.number().numberBetween(roll.darkness() + 1, MAX_LIGHT + 1));

        assertThat(passed).isFalse();
    }

    private static SlimeSpawnChecker.SurfaceRoll generatePassingRoll() {
        return new SlimeSpawnChecker.SurfaceRoll(
                (float) FAKER.random().nextDouble(0, SlimeSpawnChecker.SPAWN_CHANCE),
                FAKER.number().numberBetween(0, SlimeSpawnChecker.DARKNESS_BOUND));
    }
}
