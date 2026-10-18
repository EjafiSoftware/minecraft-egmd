package com.ejafi.egmd.dimension;

import com.ejafi.egmd.EGMD;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public final class EgmdBiomes {
    public static final ResourceKey<Biome> MINING_PLAINS = createKey("mining_plains");
    public static final ResourceKey<Biome> NETHER_MINING_WASTES = createKey("nether_mining_wastes");
    public static final ResourceKey<Biome> END_MINING_WASTES = createKey("end_mining_wastes");
    public static final ResourceKey<Biome> HAUNTED_WOODS = createKey("haunted_woods");

    private EgmdBiomes() {}

    private static ResourceKey<Biome> createKey(String path) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(EGMD.MOD_ID, path));
    }
}
