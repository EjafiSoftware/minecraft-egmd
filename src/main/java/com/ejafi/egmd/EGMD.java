package com.ejafi.egmd;

import com.ejafi.egmd.content.block.MiningPortalBlock;
import com.ejafi.egmd.content.block.PortalIgnitionHandler;
import com.ejafi.egmd.core.config.ModConfig;
import com.ejafi.egmd.core.registration.ModBlocks;
import com.ejafi.egmd.core.registration.ModDimensions;
import com.ejafi.egmd.core.registration.ModItems;
import com.ejafi.egmd.core.registration.ModPoiTypes;
import com.mojang.logging.LogUtils;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Main mod class for EGMD (Ejafi Games Mining Dimensions).
 */
@Mod(EGMD.MODID)
public class EGMD {
    public static final String MODID = "egmd";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EGMD(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModDimensions.register(modEventBus);
        ModPoiTypes.register(modEventBus);

        modContainer.registerConfig(Type.COMMON, ModConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("EGMD Common Setup");
        event.enqueueWork(() -> {
            Map<Block, MiningPortalBlock> map = new HashMap<>();
            for (DeferredBlock<MiningPortalBlock> db : ModBlocks.PORTAL_BLOCKS.values()) {
                MiningPortalBlock block = db.get();
                map.put(block.getFrameBlock(), block);
            }
            PortalIgnitionHandler.initFrameMap(map);
        });
    }
}
