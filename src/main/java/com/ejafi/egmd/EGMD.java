package com.ejafi.egmd;

import com.ejafi.egmd.content.block.PortalIgnitionHandler;
import com.ejafi.egmd.core.config.ModConfig;
import com.ejafi.egmd.core.registration.ModBlocks;
import com.ejafi.egmd.core.registration.ModDimensions;
import com.ejafi.egmd.core.registration.ModItems;
import com.ejafi.egmd.core.registration.ModPoiTypes;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

/**
 * Main mod class for EGMD (Ejafi Games Mining Dimensions).
 */
@Mod(EGMD.MODID)
public class EGMD {
    public static final String MODID = "egmd";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EGMD(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModDimensions.register(modEventBus);
        ModPoiTypes.register(modEventBus);

        modContainer.registerConfig(Type.COMMON, ModConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("EGMD Common Setup");
        event.enqueueWork(PortalIgnitionHandler::init);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModItems.PEARL_BLOCK_ITEM);
        }
    }
}
