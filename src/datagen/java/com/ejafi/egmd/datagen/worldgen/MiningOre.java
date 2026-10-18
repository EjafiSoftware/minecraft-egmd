package com.ejafi.egmd.datagen.worldgen;

import com.ejafi.egmd.Egmd;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

record MiningOre(String name, OreConfiguration configuration, int count, PlacementModifier heightRange) {
    ResourceKey<ConfiguredFeature<?, ?>> toConfiguredKey() {
        return Egmd.createKey(Registries.CONFIGURED_FEATURE, name);
    }

    ResourceKey<PlacedFeature> toPlacedKey() {
        return Egmd.createKey(Registries.PLACED_FEATURE, name);
    }
}
