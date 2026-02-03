package com.ejafi.egmd.core.registration;

import com.ejafi.egmd.EGMD;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Handles resource keys and registration for EGMD dimensions.
 */
public class ModDimensions {
    private ModDimensions() { }

    public static final DeferredRegister<DimensionType> DIMENSION_TYPE_REGISTRY =
            DeferredRegister.create(Registries.DIMENSION_TYPE, EGMD.MODID);
    public static final DeferredRegister<LevelStem> LEVEL_STEM_REGISTRY =
            DeferredRegister.create(Registries.LEVEL_STEM, EGMD.MODID);

    // Overworld Mining Dimension Keys
    public static final ResourceKey<Level> OVERWORLD_MINING_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(EGMD.MODID, "overworld_mining"));
    public static final ResourceKey<DimensionType> OVERWORLD_MINING_DIM_TYPE_KEY = ResourceKey.create(Registries.DIMENSION_TYPE,
            ResourceLocation.fromNamespaceAndPath(EGMD.MODID, "overworld_mining_type"));
    public static final ResourceKey<LevelStem> OVERWORLD_MINING_STEM_KEY = ResourceKey.create(Registries.LEVEL_STEM,
            ResourceLocation.fromNamespaceAndPath(EGMD.MODID, "overworld_mining_stem"));

    // Nether Mining Dimension Keys
    public static final ResourceKey<Level> NETHER_MINING_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(EGMD.MODID, "nether_mining"));
    public static final ResourceKey<DimensionType> NETHER_MINING_DIM_TYPE_KEY = ResourceKey.create(Registries.DIMENSION_TYPE,
            ResourceLocation.fromNamespaceAndPath(EGMD.MODID, "nether_mining_type"));
    public static final ResourceKey<LevelStem> NETHER_MINING_STEM_KEY = ResourceKey.create(Registries.LEVEL_STEM,
            ResourceLocation.fromNamespaceAndPath(EGMD.MODID, "nether_mining_stem"));

    // End Mining Dimension Keys
    public static final ResourceKey<Level> END_MINING_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(EGMD.MODID, "end_mining"));
    public static final ResourceKey<DimensionType> END_MINING_DIM_TYPE_KEY = ResourceKey.create(Registries.DIMENSION_TYPE,
            ResourceLocation.fromNamespaceAndPath(EGMD.MODID, "end_mining_type"));
    public static final ResourceKey<LevelStem> END_MINING_STEM_KEY = ResourceKey.create(Registries.LEVEL_STEM,
            ResourceLocation.fromNamespaceAndPath(EGMD.MODID, "end_mining_stem"));

    public static void register(IEventBus eventBus) {
        DIMENSION_TYPE_REGISTRY.register(eventBus);
        LEVEL_STEM_REGISTRY.register(eventBus);
    }
}
