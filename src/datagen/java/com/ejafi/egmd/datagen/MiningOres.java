package com.ejafi.egmd.datagen;

import com.ejafi.egmd.pearl.PearlBlocks;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

final class MiningOres {
    private static final RuleTest STONE = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
    private static final RuleTest DEEPSLATE = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
    private static final RuleTest NETHER_STONE = new TagMatchTest(BlockTags.BASE_STONE_NETHER);
    private static final RuleTest END_STONE = new BlockMatchTest(Blocks.END_STONE);

    private static final MiningOre COAL = new MiningOre(
            "ore_coal_mining",
            new OreConfiguration(targetStoneAndDeepslate(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE), 28),
            120,
            placeUniformly(-64, 64));
    private static final MiningOre IRON = new MiningOre(
            "ore_iron_mining",
            new OreConfiguration(targetStoneAndDeepslate(Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE), 22),
            80,
            placeTriangularly(-32, 64));
    private static final MiningOre GOLD = new MiningOre(
            "ore_gold_mining",
            new OreConfiguration(targetStoneAndDeepslate(Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE), 12),
            50,
            placeUniformly(-64, 32));
    private static final MiningOre REDSTONE = new MiningOre(
            "ore_redstone_mining",
            new OreConfiguration(targetStoneAndDeepslate(Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE), 12),
            50,
            placeUniformly(-64, 16));
    private static final MiningOre DIAMOND = new MiningOre(
            "ore_diamond_mining",
            new OreConfiguration(targetStoneAndDeepslate(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE), 12),
            40,
            placeUniformly(-64, 16));
    private static final MiningOre LAPIS = new MiningOre(
            "ore_lapis_mining",
            new OreConfiguration(targetStoneAndDeepslate(Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE), 10),
            30,
            placeTriangularly(-64, 16));
    private static final MiningOre COPPER = new MiningOre(
            "ore_copper_mining",
            new OreConfiguration(targetStoneAndDeepslate(Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE), 20),
            60,
            placeTriangularly(-16, 64));
    private static final MiningOre EMERALD = new MiningOre(
            "ore_emerald_mining",
            new OreConfiguration(targetStoneAndDeepslate(Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE), 5),
            20,
            placeUniformly(-64, 16));

    private static final MiningOre NETHER_GOLD = new MiningOre(
            "ore_gold_nether_mining",
            new OreConfiguration(NETHER_STONE, Blocks.NETHER_GOLD_ORE.defaultBlockState(), 36),
            64,
            placeUniformly(0, 128));
    private static final MiningOre NETHER_QUARTZ = new MiningOre(
            "ore_quartz_nether_mining",
            new OreConfiguration(NETHER_STONE, Blocks.NETHER_QUARTZ_ORE.defaultBlockState(), 32),
            80,
            placeUniformly(0, 128));
    private static final MiningOre ANCIENT_DEBRIS = new MiningOre(
            "ore_ancient_debris_mining",
            new OreConfiguration(NETHER_STONE, Blocks.ANCIENT_DEBRIS.defaultBlockState(), 4),
            8,
            placeUniformly(0, 128));

    private static final MiningOre PRISMARINE = new MiningOre(
            "ore_prismarine_mining",
            new OreConfiguration(
                    List.of(
                            OreConfiguration.target(
                                    new RandomBlockMatchTest(Blocks.END_STONE, 0.333F),
                                    Blocks.PRISMARINE.defaultBlockState()),
                            OreConfiguration.target(
                                    new RandomBlockMatchTest(Blocks.END_STONE, 0.5F),
                                    Blocks.PRISMARINE_BRICKS.defaultBlockState()),
                            OreConfiguration.target(END_STONE, Blocks.DARK_PRISMARINE.defaultBlockState())),
                    32),
            64,
            placeUniformly(1, 64));
    private static final MiningOre SEA_LANTERN = new MiningOre(
            "ore_sea_lantern_mining",
            new OreConfiguration(END_STONE, Blocks.SEA_LANTERN.defaultBlockState(), 12),
            48,
            placeUniformly(1, 64));
    private static final MiningOre PEARL_BLOCK = new MiningOre(
            "ore_pearl_block_mining",
            new OreConfiguration(END_STONE, PearlBlocks.PEARL_BLOCK.get().defaultBlockState(), 4),
            4,
            placeUniformly(1, 64));

    static final List<MiningOre> OVERWORLD_ORES = List.of(COAL, IRON, GOLD, REDSTONE, DIAMOND, LAPIS, COPPER, EMERALD);
    static final List<MiningOre> NETHER_ORES = List.of(NETHER_GOLD, NETHER_QUARTZ, ANCIENT_DEBRIS);
    static final List<MiningOre> END_ORES = List.of(PRISMARINE, SEA_LANTERN, PEARL_BLOCK);

    private static final List<MiningOre> ALL = Stream.of(OVERWORLD_ORES, NETHER_ORES, END_ORES)
            .flatMap(List::stream)
            .toList();

    private MiningOres() {}

    static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        for (MiningOre ore : ALL) {
            context.register(ore.toConfiguredKey(), new ConfiguredFeature<>(Feature.ORE, ore.configuration()));
        }
    }

    static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        for (MiningOre ore : ALL) {
            context.register(
                    ore.toPlacedKey(),
                    new PlacedFeature(
                            configuredFeatures.getOrThrow(ore.toConfiguredKey()),
                            List.of(
                                    CountPlacement.of(ore.count()),
                                    InSquarePlacement.spread(),
                                    ore.heightRange(),
                                    BiomeFilter.biome())));
        }
    }

    private static List<OreConfiguration.TargetBlockState> targetStoneAndDeepslate(Block stoneOre, Block deepslateOre) {
        return List.of(
                OreConfiguration.target(STONE, stoneOre.defaultBlockState()),
                OreConfiguration.target(DEEPSLATE, deepslateOre.defaultBlockState()));
    }

    private static HeightRangePlacement placeUniformly(int minY, int maxY) {
        return HeightRangePlacement.uniform(VerticalAnchor.absolute(minY), VerticalAnchor.absolute(maxY));
    }

    private static HeightRangePlacement placeTriangularly(int minY, int maxY) {
        return HeightRangePlacement.triangle(VerticalAnchor.absolute(minY), VerticalAnchor.absolute(maxY));
    }
}
