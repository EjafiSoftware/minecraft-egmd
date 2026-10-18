package com.ejafi.egmd;

import com.ejafi.egmd.pearl.PearlBlocks;
import com.ejafi.egmd.portal.PortalConfig;
import com.ejafi.egmd.portal.PortalRegisters;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(Egmd.MOD_ID)
public final class Egmd {
    public static final String MOD_ID = "egmd";

    public Egmd(IEventBus modEventBus, ModContainer modContainer) {
        PortalRegisters.register(modEventBus);
        PearlBlocks.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, PortalConfig.SPEC);
    }

    public static ResourceLocation createLocation(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static <T> ResourceKey<T> createKey(ResourceKey<? extends Registry<T>> registry, String path) {
        return ResourceKey.create(registry, createLocation(path));
    }
}
