package com.ejafi.egmd.dimension;

import com.ejafi.egmd.Egmd;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public final class EgmdBiomes {
    public static final ResourceKey<Biome> MINING_PLAINS = Egmd.createKey(Registries.BIOME, "mining_plains");
    public static final ResourceKey<Biome> NETHER_MINING_WASTES =
            Egmd.createKey(Registries.BIOME, "nether_mining_wastes");
    public static final ResourceKey<Biome> END_MINING_WASTES = Egmd.createKey(Registries.BIOME, "end_mining_wastes");
    public static final ResourceKey<Biome> HAUNTED_WOODS = Egmd.createKey(Registries.BIOME, "haunted_woods");

    private EgmdBiomes() {}
}
