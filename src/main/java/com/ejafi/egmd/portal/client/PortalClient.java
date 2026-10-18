package com.ejafi.egmd.portal.client;

import com.ejafi.egmd.Egmd;
import com.ejafi.egmd.portal.PortalRegisters;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Egmd.MOD_ID, dist = Dist.CLIENT)
public final class PortalClient {
    public PortalClient(ModContainer container, IEventBus modEventBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modEventBus.addListener(this::registerBlockColors);
        modEventBus.addListener(this::registerParticleProviders);
    }

    private void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(PortalRegisters.PORTAL_SPARK.get(), PortalSparkParticle.Provider::new);
    }

    private void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        PortalRegisters.PORTAL_BLOCKS.forEach((variant, block) -> {
            int tint = variant.tintColor();
            event.register((state, level, pos, tintIndex) -> tint, block.get());
        });
    }
}
