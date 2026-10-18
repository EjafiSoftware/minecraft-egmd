package com.ejafi.egmd.dimension;

import java.util.List;
import java.util.stream.Stream;
import net.datafaker.Faker;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;

final class FlatTerrainFaker {
    private static final Faker FAKER = new Faker();
    private static final int MAX_LAYER_COUNT = 4;
    private static final int MAX_LAYER_HEIGHT = 64;
    private static final List<Block> LAYER_BLOCKS = List.of(
            Blocks.BEDROCK,
            Blocks.DEEPSLATE,
            Blocks.STONE,
            Blocks.DIRT,
            Blocks.GRASS_BLOCK,
            Blocks.NETHERRACK,
            Blocks.END_STONE);

    private int minY = FAKER.number().numberBetween(DimensionType.MIN_Y, 0);
    private List<FlatLayerInfo> layers = Stream.generate(
                    () -> generateLayer(FAKER.number().numberBetween(1, MAX_LAYER_HEIGHT + 1)))
            .limit(FAKER.number().numberBetween(1, MAX_LAYER_COUNT + 1))
            .toList();

    static FlatLayerInfo generateLayer(int height) {
        return new FlatLayerInfo(height, FAKER.options().nextElement(LAYER_BLOCKS));
    }

    FlatTerrainFaker withMinY(int minY) {
        this.minY = minY;
        return this;
    }

    FlatTerrainFaker withLayers(List<FlatLayerInfo> layers) {
        this.layers = layers;
        return this;
    }

    Terrain.Flat generate() {
        return new Terrain.Flat(minY, layers);
    }
}
