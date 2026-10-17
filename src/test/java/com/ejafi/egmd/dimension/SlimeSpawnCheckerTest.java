package com.ejafi.egmd.dimension;

import static org.assertj.core.api.Assertions.assertThat;

import net.datafaker.Faker;
import net.minecraft.world.level.dimension.DimensionType;
import org.junit.jupiter.api.Test;

class SlimeSpawnCheckerTest {
    private static final Faker FAKER = new Faker();
    private static final int MAX_LIGHT = 15;

    @Test
    void admits_acceptsSpawn_whenDarkSpotIsInSurfaceBand() {
        SlimeSpawnChecker.SurfaceRoll roll = generatePassingRoll();

        boolean admitted = roll.admits(generateSurfaceY(), FAKER.number().numberBetween(0, roll.darkness() + 1));

        assertThat(admitted).isTrue();
    }

    @Test
    void admits_refusesSpawn_whenSpotIsBelowSurfaceBand() {
        int y = FAKER.number().numberBetween(DimensionType.MIN_Y, SlimeSpawnChecker.SURFACE_MIN_Y);

        boolean admitted = generatePassingRoll().admits(y, 0);

        assertThat(admitted).isFalse();
    }

    @Test
    void admits_refusesSpawn_whenSpotIsAboveSurfaceBand() {
        int y = FAKER.number().numberBetween(SlimeSpawnChecker.SURFACE_MAX_Y + 1, DimensionType.MAX_Y);

        boolean admitted = generatePassingRoll().admits(y, 0);

        assertThat(admitted).isFalse();
    }

    @Test
    void admits_refusesSpawn_whenChanceRollFails() {
        SlimeSpawnChecker.SurfaceRoll roll = new SlimeSpawnChecker.SurfaceRoll(
                (float) FAKER.random().nextDouble(SlimeSpawnChecker.SPAWN_CHANCE, 1),
                FAKER.number().numberBetween(0, SlimeSpawnChecker.DARKNESS_BOUND));

        boolean admitted = roll.admits(generateSurfaceY(), 0);

        assertThat(admitted).isFalse();
    }

    @Test
    void admits_refusesSpawn_whenLightExceedsDarknessRoll() {
        SlimeSpawnChecker.SurfaceRoll roll = generatePassingRoll();

        boolean admitted =
                roll.admits(generateSurfaceY(), FAKER.number().numberBetween(roll.darkness() + 1, MAX_LIGHT + 1));

        assertThat(admitted).isFalse();
    }

    private static SlimeSpawnChecker.SurfaceRoll generatePassingRoll() {
        return new SlimeSpawnChecker.SurfaceRoll(
                (float) FAKER.random().nextDouble(0, SlimeSpawnChecker.SPAWN_CHANCE),
                FAKER.number().numberBetween(0, SlimeSpawnChecker.DARKNESS_BOUND));
    }

    private static int generateSurfaceY() {
        return FAKER.number().numberBetween(SlimeSpawnChecker.SURFACE_MIN_Y, SlimeSpawnChecker.SURFACE_MAX_Y + 1);
    }
}
