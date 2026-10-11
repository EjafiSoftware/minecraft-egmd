package com.ejafi.egmd.datagen;

import com.ejafi.egmd.dimension.MiningDimension;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

final class MiningLevels {
    private static final List<MiningLevel> ALL = List.of(
            new MiningLevel(
                    MiningDimension.OVERWORLD,
                    MiningBiomes.MINING_PLAINS,
                    createOverworldType(MiningDimension.OVERWORLD.minY())),
            new MiningLevel(
                    MiningDimension.NETHER,
                    MiningBiomes.NETHER_MINING_WASTES,
                    createNetherType(MiningDimension.NETHER.minY())),
            new MiningLevel(
                    MiningDimension.END, MiningBiomes.END_MINING_WASTES, createEndType(MiningDimension.END.minY())));

    private MiningLevels() {}

    static void bootstrapDimensionTypes(BootstrapContext<DimensionType> context) {
        for (MiningLevel level : ALL) {
            context.register(level.toTypeKey(), level.type());
        }
    }

    static void bootstrapLevelStems(BootstrapContext<LevelStem> context) {
        HolderGetter<DimensionType> types = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        for (MiningLevel level : ALL) {
            Holder<Biome> biome = biomes.getOrThrow(level.biome());
            FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(Optional.empty(), biome, List.of())
                    .withBiomeAndLayers(level.dimension().layers(), Optional.empty(), biome);
            settings.setDecoration();
            context.register(
                    level.toStemKey(),
                    new LevelStem(types.getOrThrow(level.toTypeKey()), new FlatLevelSource(settings)));
        }
    }

    private static DimensionType createOverworldType(int minY) {
        return new DimensionType(
                OptionalLong.of(6000L),
                true,
                false,
                false,
                true,
                1.0,
                false,
                false,
                minY,
                384,
                384,
                BlockTags.INFINIBURN_OVERWORLD,
                BuiltinDimensionTypes.OVERWORLD_EFFECTS,
                0.0F,
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0));
    }

    private static DimensionType createNetherType(int minY) {
        return new DimensionType(
                OptionalLong.empty(),
                false,
                false,
                true,
                false,
                8.0,
                false,
                true,
                minY,
                256,
                256,
                BlockTags.INFINIBURN_NETHER,
                BuiltinDimensionTypes.NETHER_EFFECTS,
                1.0F,
                new DimensionType.MonsterSettings(true, false, ConstantInt.of(15), 15));
    }

    private static DimensionType createEndType(int minY) {
        return new DimensionType(
                OptionalLong.empty(),
                false,
                false,
                false,
                false,
                1.0,
                false,
                false,
                minY,
                256,
                256,
                BlockTags.INFINIBURN_END,
                BuiltinDimensionTypes.END_EFFECTS,
                1.0F,
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(15), 15));
    }

    private record MiningLevel(MiningDimension dimension, ResourceKey<Biome> biome, DimensionType type) {
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
