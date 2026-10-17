package com.ejafi.egmd.datagen;

import com.ejafi.egmd.dimension.EgmdDimension;
import com.ejafi.egmd.dimension.EgmdDimensions;
import com.ejafi.egmd.dimension.Restriction;
import com.ejafi.egmd.dimension.Terrain;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

final class EgmdLevels {
    private static final List<LevelDefinition> ALL = List.of(
            new LevelDefinition(
                    EgmdDimensions.OVERWORLD_MINING, MiningBiomes.MINING_PLAINS, EgmdLevels::createOverworldMiningType),
            new LevelDefinition(
                    EgmdDimensions.NETHER_MINING,
                    MiningBiomes.NETHER_MINING_WASTES,
                    EgmdLevels::createNetherMiningType),
            new LevelDefinition(
                    EgmdDimensions.END_MINING, MiningBiomes.END_MINING_WASTES, EgmdLevels::createEndMiningType),
            new LevelDefinition(EgmdDimensions.HAUNTED, HauntedBiomes.HAUNTED_WOODS, EgmdLevels::createHauntedType));

    private EgmdLevels() {}

    static void bootstrapDimensionTypes(BootstrapContext<DimensionType> context) {
        for (LevelDefinition level : ALL) {
            context.register(level.toTypeKey(), level.typeFactory().apply(level.dimension()));
        }
    }

    static void bootstrapLevelStems(BootstrapContext<LevelStem> context) {
        HolderGetter<DimensionType> types = context.lookup(Registries.DIMENSION_TYPE);
        for (LevelDefinition level : ALL) {
            context.register(
                    level.toStemKey(),
                    new LevelStem(types.getOrThrow(level.toTypeKey()), createGenerator(context, level)));
        }
    }

    private static ChunkGenerator createGenerator(BootstrapContext<LevelStem> context, LevelDefinition level) {
        Holder<Biome> biome = context.lookup(Registries.BIOME).getOrThrow(level.biome());
        return switch (level.dimension().terrain()) {
            case Terrain.Flat flat -> {
                FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(Optional.empty(), biome, List.of())
                        .withBiomeAndLayers(flat.layers(), Optional.empty(), biome);
                settings.setDecoration();
                yield new FlatLevelSource(settings);
            }
            case Terrain.Generated generated ->
                new NoiseBasedChunkGenerator(
                        new FixedBiomeSource(biome),
                        context.lookup(Registries.NOISE_SETTINGS).getOrThrow(generated.noiseSettings()));
        };
    }

    private static DimensionType createOverworldMiningType(EgmdDimension dimension) {
        return new DimensionType(
                OptionalLong.of(6000L),
                true,
                false,
                false,
                true,
                1.0,
                allowsBeds(dimension),
                false,
                dimension.terrain().minY(),
                384,
                384,
                BlockTags.INFINIBURN_OVERWORLD,
                BuiltinDimensionTypes.OVERWORLD_EFFECTS,
                0.0F,
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0));
    }

    private static DimensionType createNetherMiningType(EgmdDimension dimension) {
        return new DimensionType(
                OptionalLong.empty(),
                false,
                false,
                true,
                false,
                8.0,
                allowsBeds(dimension),
                true,
                dimension.terrain().minY(),
                256,
                256,
                BlockTags.INFINIBURN_NETHER,
                BuiltinDimensionTypes.NETHER_EFFECTS,
                1.0F,
                new DimensionType.MonsterSettings(true, false, ConstantInt.of(15), 15));
    }

    private static DimensionType createEndMiningType(EgmdDimension dimension) {
        return new DimensionType(
                OptionalLong.empty(),
                false,
                false,
                false,
                false,
                1.0,
                allowsBeds(dimension),
                false,
                dimension.terrain().minY(),
                256,
                256,
                BlockTags.INFINIBURN_END,
                BuiltinDimensionTypes.END_EFFECTS,
                1.0F,
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(15), 15));
    }

    private static DimensionType createHauntedType(EgmdDimension dimension) {
        return new DimensionType(
                OptionalLong.of(18000L),
                true,
                false,
                false,
                true,
                1.0,
                allowsBeds(dimension),
                false,
                dimension.terrain().minY(),
                384,
                384,
                BlockTags.INFINIBURN_OVERWORLD,
                BuiltinDimensionTypes.OVERWORLD_EFFECTS,
                0.0F,
                new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 7), 0));
    }

    private static boolean allowsBeds(EgmdDimension dimension) {
        return !dimension.restrictions().contains(Restriction.NO_BEDS);
    }

    private record LevelDefinition(
            EgmdDimension dimension, ResourceKey<Biome> biome, Function<EgmdDimension, DimensionType> typeFactory) {
        ResourceKey<DimensionType> toTypeKey() {
            return ResourceKey.create(
                    Registries.DIMENSION_TYPE, dimension.levelKey().location().withSuffix("_type"));
        }

        ResourceKey<LevelStem> toStemKey() {
            return ResourceKey.create(
                    Registries.LEVEL_STEM, dimension.levelKey().location());
        }
    }
}
