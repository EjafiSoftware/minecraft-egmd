package com.ejafi.egmd.client;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.core.registration.ModBlocks;
import com.ejafi.egmd.core.registration.ModParticles;
import com.ejafi.egmd.core.registration.PortalVariant;
import com.ejafi.egmd.core.registration.PortalVariants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client-side setup for the EGMD mod.
 */
@Mod(value = EGMD.MODID, dist = Dist.CLIENT)
public class EGMDClient {
    public EGMDClient(ModContainer container, IEventBus modEventBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::registerBlockColors);
        modEventBus.addListener(this::registerParticleProviders);
    }

    private void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.PORTAL_SPARK.get(), PortalSparkParticle.Provider::new);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        EGMD.LOGGER.info("EGMD Client Setup");
    }

    private void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        for (PortalVariant v : PortalVariants.ALL) {
            int tint = v.tintColor();
            event.register((state, level, pos, tintIndex) -> tint,
                    ModBlocks.PORTAL_BLOCKS.get(v).get());
        }
    }
}
