package com.ejafi.egmd.datagen;

import com.ejafi.egmd.dimension.EgmdBiomes;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.CavePlacements;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

final class MiningBiomes {
    private static final int WATER_COLOR = 0x3F76FF;
    private static final int WATER_FOG_COLOR = 0x050533;
    private static final List<ResourceKey<PlacedFeature>> VANILLA_OVERWORLD_ORES = List.of(
            OrePlacements.ORE_DIRT,
            OrePlacements.ORE_GRAVEL,
            OrePlacements.ORE_GRANITE_UPPER,
            OrePlacements.ORE_GRANITE_LOWER,
            OrePlacements.ORE_DIORITE_UPPER,
            OrePlacements.ORE_DIORITE_LOWER,
            OrePlacements.ORE_ANDESITE_UPPER,
            OrePlacements.ORE_ANDESITE_LOWER,
            OrePlacements.ORE_TUFF);
    private static final List<ResourceKey<PlacedFeature>> VANILLA_NETHER_ORES = List.of(
            OrePlacements.ORE_MAGMA,
            OrePlacements.ORE_SOUL_SAND,
            OrePlacements.ORE_GOLD_NETHER,
            OrePlacements.ORE_QUARTZ_NETHER);

    private MiningBiomes() {}

    static void bootstrap(BootstrapContext<Biome> context) {
        context.register(EgmdBiomes.MINING_PLAINS, createMiningPlains(context));
        context.register(EgmdBiomes.NETHER_MINING_WASTES, createNetherMiningWastes(context));
        context.register(EgmdBiomes.END_MINING_WASTES, createEndMiningWastes(context));
    }

    private static Biome createMiningPlains(BootstrapContext<Biome> context) {
        return startBiome()
                .hasPrecipitation(true)
                .temperature(0.8F)
                .downfall(0.4F)
                .specialEffects(createEffects(0x78A7FF, 0xC0D8FF)
                        .grassColorOverride(0x90814D)
                        .foliageColorOverride(0x758369)
                        .build())
                .generationSettings(createOreGeneration(
                                context, listOrePlacements(MiningOres.OVERWORLD_ORES, VANILLA_OVERWORLD_ORES))
                        .addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, CavePlacements.AMETHYST_GEODE)
                        .build())
                .build();
    }

    private static Biome createNetherMiningWastes(BootstrapContext<Biome> context) {
        return startBiome()
                .hasPrecipitation(false)
                .temperature(2.0F)
                .downfall(0.0F)
                .specialEffects(createEffects(0x6EB1FF, 0x330808).build())
                .generationSettings(
                        createOreGeneration(context, listOrePlacements(MiningOres.NETHER_ORES, VANILLA_NETHER_ORES))
                                .build())
                .build();
    }

    private static Biome createEndMiningWastes(BootstrapContext<Biome> context) {
        return startBiome()
                .hasPrecipitation(false)
                .temperature(0.5F)
                .downfall(0.5F)
                .specialEffects(createEffects(0x000000, 0x100CDC).build())
                .generationSettings(createOreGeneration(context, listOrePlacements(MiningOres.END_ORES, List.of()))
                        .build())
                .build();
    }

    private static Biome.BiomeBuilder startBiome() {
        return new Biome.BiomeBuilder().mobSpawnSettings(MobSpawnSettings.EMPTY);
    }

    private static BiomeSpecialEffects.Builder createEffects(int skyColor, int fogColor) {
        return new BiomeSpecialEffects.Builder()
                .skyColor(skyColor)
                .fogColor(fogColor)
                .waterColor(WATER_COLOR)
                .waterFogColor(WATER_FOG_COLOR)
                .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS);
    }

    private static BiomeGenerationSettings.Builder createOreGeneration(
            BootstrapContext<Biome> context, List<ResourceKey<PlacedFeature>> ores) {
        var generation = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE), context.lookup(Registries.CONFIGURED_CARVER));
        ores.forEach(ore -> generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, ore));
        return generation;
    }

    private static List<ResourceKey<PlacedFeature>> listOrePlacements(
            List<MiningOre> modOres, List<ResourceKey<PlacedFeature>> vanillaOres) {
        return Stream.concat(modOres.stream().map(MiningOre::toPlacedKey), vanillaOres.stream())
                .toList();
    }
}
