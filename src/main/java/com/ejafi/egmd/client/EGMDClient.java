package com.ejafi.egmd.client;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.core.registration.ModBlocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client-side setup for the EGMD mod.
 */
@Mod(value = EGMD.MODID, dist = Dist.CLIENT)
public class EGMDClient {
    public EGMDClient(ModContainer container, IEventBus modEventBus) {
        // Register the config screen factory
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::registerBlockColors);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        EGMD.LOGGER.info("EGMD Client Setup");
    }

    private void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        // Apply tints to portal blocks
        event.register((state, level, pos, tintIndex) -> 0x00FF00, ModBlocks.MINING_PORTAL.get());
        event.register((state, level, pos, tintIndex) -> 0xFF0000, ModBlocks.NETHER_MINING_PORTAL.get());
        event.register((state, level, pos, tintIndex) -> 0xFFFF00, ModBlocks.END_MINING_PORTAL.get());
    }
}
