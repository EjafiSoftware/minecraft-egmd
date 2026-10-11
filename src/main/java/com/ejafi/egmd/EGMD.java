package com.ejafi.egmd;

import com.ejafi.egmd.pearl.PearlBlocks;
import com.ejafi.egmd.portal.PortalConfig;
import com.ejafi.egmd.portal.PortalRegisters;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(EGMD.MOD_ID)
public final class EGMD {
    public static final String MOD_ID = "egmd";

    public EGMD(IEventBus modEventBus, ModContainer modContainer) {
        PortalRegisters.register(modEventBus);
        PearlBlocks.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, PortalConfig.SPEC);
    }
}
