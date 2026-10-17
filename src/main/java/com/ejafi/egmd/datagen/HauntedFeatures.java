package com.ejafi.egmd.datagen;

import com.ejafi.egmd.EGMD;
import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedBlockStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

final class HauntedFeatures {
    static final ResourceKey<PlacedFeature> PUMPKIN_PATCH = createPlacedKey("haunted_pumpkin_patch");
    static final ResourceKey<PlacedFeature> CARVED_PUMPKIN_PATCH = createPlacedKey("haunted_carved_pumpkin_patch");
    static final ResourceKey<PlacedFeature> JACK_O_LANTERN_PATCH = createPlacedKey("haunted_jack_o_lantern_patch");
    static final ResourceKey<PlacedFeature> COBWEB_PATCH = createPlacedKey("haunted_cobweb_patch");
    static final ResourceKey<PlacedFeature> SOUL_SAND_DISK = createPlacedKey("haunted_soul_sand_disk");

    private static final ResourceKey<ConfiguredFeature<?, ?>> CARVED_PUMPKIN_PATCH_FEATURE =
            createConfiguredKey("haunted_carved_pumpkin_patch");
    private static final ResourceKey<ConfiguredFeature<?, ?>> JACK_O_LANTERN_PATCH_FEATURE =
            createConfiguredKey("haunted_jack_o_lantern_patch");
    private static final ResourceKey<ConfiguredFeature<?, ?>> COBWEB_PATCH_FEATURE =
            createConfiguredKey("haunted_cobweb_patch");
    private static final ResourceKey<ConfiguredFeature<?, ?>> SOUL_SAND_DISK_FEATURE =
            createConfiguredKey("haunted_soul_sand_disk");

    private static final int SPARSE_PATCH_TRIES = 12;
    private static final int DENSE_PATCH_TRIES = 48;

    private HauntedFeatures() {}

    static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        context.register(CARVED_PUMPKIN_PATCH_FEATURE, createGrassPatch(Blocks.CARVED_PUMPKIN, SPARSE_PATCH_TRIES));
        context.register(JACK_O_LANTERN_PATCH_FEATURE, createGrassPatch(Blocks.JACK_O_LANTERN, SPARSE_PATCH_TRIES));
        context.register(COBWEB_PATCH_FEATURE, createGrassPatch(Blocks.COBWEB, DENSE_PATCH_TRIES));
        context.register(
                SOUL_SAND_DISK_FEATURE,
                new ConfiguredFeature<>(
                        Feature.DISK,
                        new DiskConfiguration(
                                RuleBasedBlockStateProvider.simple(Blocks.SOUL_SAND),
                                BlockPredicate.matchesBlocks(List.of(Blocks.GRASS_BLOCK, Blocks.DIRT)),
                                UniformInt.of(2, 4),
                                2)));
    }

    static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> features = context.lookup(Registries.CONFIGURED_FEATURE);
        context.register(
                PUMPKIN_PATCH,
                new PlacedFeature(features.getOrThrow(VegetationFeatures.PATCH_PUMPKIN), placeOnSurfaceOnceEvery(6)));
        context.register(
                CARVED_PUMPKIN_PATCH,
                new PlacedFeature(features.getOrThrow(CARVED_PUMPKIN_PATCH_FEATURE), placeOnSurfaceOnceEvery(10)));
        context.register(
                JACK_O_LANTERN_PATCH,
                new PlacedFeature(features.getOrThrow(JACK_O_LANTERN_PATCH_FEATURE), placeOnSurfaceOnceEvery(32)));
        context.register(
                COBWEB_PATCH, new PlacedFeature(features.getOrThrow(COBWEB_PATCH_FEATURE), placeOnSurfaceOnceEvery(2)));
        context.register(
                SOUL_SAND_DISK,
                new PlacedFeature(
                        features.getOrThrow(SOUL_SAND_DISK_FEATURE),
                        List.of(
                                RarityFilter.onAverageOnceEvery(6),
                                InSquarePlacement.spread(),
                                PlacementUtils.HEIGHTMAP_TOP_SOLID,
                                BiomeFilter.biome())));
    }

    private static ConfiguredFeature<?, ?> createGrassPatch(Block block, int tries) {
        return new ConfiguredFeature<>(
                Feature.RANDOM_PATCH,
                FeatureUtils.simplePatchConfiguration(
                        Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(BlockStateProvider.simple(block)),
                        List.of(Blocks.GRASS_BLOCK),
                        tries));
    }

    private static List<PlacementModifier> placeOnSurfaceOnceEvery(int chunks) {
        return List.of(
                RarityFilter.onAverageOnceEvery(chunks),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP,
                BiomeFilter.biome());
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> createConfiguredKey(String path) {
        return ResourceKey.create(
                Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(EGMD.MOD_ID, path));
    }

    private static ResourceKey<PlacedFeature> createPlacedKey(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(EGMD.MOD_ID, path));
    }
}
