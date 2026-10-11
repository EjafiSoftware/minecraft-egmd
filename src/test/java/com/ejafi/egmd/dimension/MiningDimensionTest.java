package com.ejafi.egmd.dimension;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.datafaker.Faker;
import net.minecraft.world.level.dimension.DimensionType;
import org.junit.jupiter.api.Test;

class MiningDimensionTest {
    private static final Faker FAKER = new Faker();

    @Test
    void computeSurfaceY_returnsFirstYAboveTopLayer_whenLayersStackUpFromMinY() {
        int minY = FAKER.number().numberBetween(DimensionType.MIN_Y, 0);
        int depth = FAKER.number().numberBetween(2, 256);
        int topHeight = FAKER.number().numberBetween(1, depth);
        MiningDimension dimension = new MiningDimensionFaker()
                .withMinY(minY)
                .withLayers(List.of(
                        MiningDimensionFaker.generateLayer(depth - topHeight),
                        MiningDimensionFaker.generateLayer(topHeight)))
                .generate();

        int surfaceY = dimension.computeSurfaceY();

        assertThat(surfaceY).isEqualTo(minY + depth);
    }
}
