package com.ejafi.egmd.datagen;

import com.ejafi.egmd.EGMD;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

record MiningOre(String name, OreConfiguration configuration, int count, PlacementModifier heightRange) {
    ResourceKey<ConfiguredFeature<?, ?>> toConfiguredKey() {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, toLocation());
    }

    ResourceKey<PlacedFeature> toPlacedKey() {
        return ResourceKey.create(Registries.PLACED_FEATURE, toLocation());
    }

    private ResourceLocation toLocation() {
        return ResourceLocation.fromNamespaceAndPath(EGMD.MOD_ID, name);
    }
}
