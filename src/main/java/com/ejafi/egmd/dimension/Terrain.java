package com.ejafi.egmd.dimension;

import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;

public sealed interface Terrain {
    int minY();

    record Flat(int minY, List<FlatLayerInfo> layers) implements Terrain {
        public Flat {
            layers = List.copyOf(layers);
        }

        public int computeSurfaceY() {
            return minY + layers.stream().mapToInt(FlatLayerInfo::getHeight).sum();
        }
    }

    record Generated(int minY, ResourceKey<NoiseGeneratorSettings> noiseSettings) implements Terrain {}
}
