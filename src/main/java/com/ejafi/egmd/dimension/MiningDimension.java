package com.ejafi.egmd.dimension;

import com.ejafi.egmd.EGMD;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;

public record MiningDimension(ResourceKey<Level> levelKey, int minY, List<FlatLayerInfo> layers) {
    public static final MiningDimension OVERWORLD = new MiningDimension(
            createLevelKey("overworld_mining"),
            -64,
            List.of(
                    new FlatLayerInfo(1, Blocks.BEDROCK),
                    new FlatLayerInfo(64, Blocks.DEEPSLATE),
                    new FlatLayerInfo(64, Blocks.STONE),
                    new FlatLayerInfo(2, Blocks.DIRT),
                    new FlatLayerInfo(1, Blocks.GRASS_BLOCK)));

    public static final MiningDimension NETHER = new MiningDimension(
            createLevelKey("nether_mining"),
            0,
            List.of(new FlatLayerInfo(1, Blocks.BEDROCK), new FlatLayerInfo(128, Blocks.NETHERRACK)));

    public static final MiningDimension END = new MiningDimension(
            createLevelKey("end_mining"),
            0,
            List.of(new FlatLayerInfo(1, Blocks.BEDROCK), new FlatLayerInfo(64, Blocks.END_STONE)));

    static final List<MiningDimension> ALL = List.of(OVERWORLD, NETHER, END);

    private static final Map<ResourceKey<Level>, MiningDimension> BY_LEVEL_KEY =
            ALL.stream().collect(Collectors.toUnmodifiableMap(MiningDimension::levelKey, Function.identity()));

    public MiningDimension {
        layers = List.copyOf(layers);
    }

    public int computeSurfaceY() {
        return minY + layers.stream().mapToInt(FlatLayerInfo::getHeight).sum();
    }

    private static ResourceKey<Level> createLevelKey(String path) {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(EGMD.MOD_ID, path));
    }

    public static Optional<MiningDimension> findByLevelKey(ResourceKey<Level> levelKey) {
        return Optional.ofNullable(BY_LEVEL_KEY.get(levelKey));
    }
}
