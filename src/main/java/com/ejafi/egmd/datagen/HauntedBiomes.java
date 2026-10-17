package com.ejafi.egmd.datagen;

import com.ejafi.egmd.EGMD;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;

final class HauntedBiomes {
    static final ResourceKey<Biome> HAUNTED_WOODS =
            ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(EGMD.MOD_ID, "haunted_woods"));

    private static final float ASH_DENSITY = 0.00625F;

    private HauntedBiomes() {}

    static void bootstrap(BootstrapContext<Biome> context) {
        context.register(HAUNTED_WOODS, createHauntedWoods(context));
    }

    private static Biome createHauntedWoods(BootstrapContext<Biome> context) {
        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(0.6F)
                .downfall(0.8F)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .skyColor(0x0B0614)
                        .fogColor(0x1A0F24)
                        .waterColor(0x3F2A55)
                        .waterFogColor(0x0E0716)
                        .grassColorOverride(0x4A3B2A)
                        .foliageColorOverride(0x3B2F1E)
                        .ambientParticle(new AmbientParticleSettings(ParticleTypes.ASH, ASH_DENSITY))
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build())
                .mobSpawnSettings(createSpawns())
                .generationSettings(createGeneration(context))
                .build();
    }

    private static MobSpawnSettings createSpawns() {
        return new MobSpawnSettings.Builder()
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SPIDER, 100, 2, 4))
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 100, 2, 4))
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE, 40, 2, 4))
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 40, 2, 4))
                .addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.BAT, 20, 8, 8))
                .build();
    }

    private static BiomeGenerationSettings createGeneration(BootstrapContext<Biome> context) {
        var generation = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE), context.lookup(Registries.CONFIGURED_CARVER));
        BiomeDefaultFeatures.addDefaultCarversAndLakes(generation);
        BiomeDefaultFeatures.addDefaultCrystalFormations(generation);
        BiomeDefaultFeatures.addDefaultMonsterRoom(generation);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(generation);
        BiomeDefaultFeatures.addDefaultSprings(generation);
        BiomeDefaultFeatures.addFossilDecoration(generation);
        BiomeDefaultFeatures.addDefaultOres(generation);
        BiomeDefaultFeatures.addDefaultSoftDisks(generation);
        generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, HauntedFeatures.SOUL_SAND_DISK);
        generation.addFeature(
                GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.DARK_FOREST_VEGETATION);
        BiomeDefaultFeatures.addForestGrass(generation);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, HauntedFeatures.PUMPKIN_PATCH);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, HauntedFeatures.CARVED_PUMPKIN_PATCH);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, HauntedFeatures.JACK_O_LANTERN_PATCH);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, HauntedFeatures.COBWEB_PATCH);
        return generation.build();
    }
}
