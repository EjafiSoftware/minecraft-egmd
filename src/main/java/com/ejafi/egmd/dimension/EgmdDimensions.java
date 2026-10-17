package com.ejafi.egmd.dimension;

import com.ejafi.egmd.EGMD;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;

public final class EgmdDimensions {
    private static final Set<Restriction> MINING_RESTRICTIONS =
            Set.of(Restriction.NO_HOSTILE_MOBS, Restriction.NO_BEDS);

    public static final EgmdDimension OVERWORLD_MINING = new EgmdDimension(
            createLevelKey("overworld_mining"),
            new Terrain.Flat(
                    -64,
                    List.of(
                            new FlatLayerInfo(1, Blocks.BEDROCK),
                            new FlatLayerInfo(64, Blocks.DEEPSLATE),
                            new FlatLayerInfo(64, Blocks.STONE),
                            new FlatLayerInfo(2, Blocks.DIRT),
                            new FlatLayerInfo(1, Blocks.GRASS_BLOCK))),
            MINING_RESTRICTIONS);

    public static final EgmdDimension NETHER_MINING = new EgmdDimension(
            createLevelKey("nether_mining"),
            new Terrain.Flat(
                    0, List.of(new FlatLayerInfo(1, Blocks.BEDROCK), new FlatLayerInfo(128, Blocks.NETHERRACK))),
            MINING_RESTRICTIONS);

    public static final EgmdDimension END_MINING = new EgmdDimension(
            createLevelKey("end_mining"),
            new Terrain.Flat(0, List.of(new FlatLayerInfo(1, Blocks.BEDROCK), new FlatLayerInfo(64, Blocks.END_STONE))),
            MINING_RESTRICTIONS);

    public static final EgmdDimension HAUNTED = new EgmdDimension(
            createLevelKey("haunted"),
            new Terrain.Generated(-64, NoiseGeneratorSettings.OVERWORLD),
            Set.of(Restriction.NO_BEDS));

    static final List<EgmdDimension> ALL = List.of(OVERWORLD_MINING, NETHER_MINING, END_MINING, HAUNTED);

    private static final Map<ResourceKey<Level>, EgmdDimension> BY_LEVEL_KEY =
            ALL.stream().collect(Collectors.toUnmodifiableMap(EgmdDimension::levelKey, Function.identity()));

    private EgmdDimensions() {}

    public static Optional<EgmdDimension> find(ResourceKey<Level> levelKey) {
        return Optional.ofNullable(BY_LEVEL_KEY.get(levelKey));
    }

    public static Set<Restriction> getRestrictions(ResourceKey<Level> levelKey) {
        return find(levelKey).map(EgmdDimension::restrictions).orElse(Set.of());
    }

    private static ResourceKey<Level> createLevelKey(String path) {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(EGMD.MOD_ID, path));
    }
}
