package com.ejafi.egmd.core.registration;

import com.ejafi.egmd.EGMD;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Handles item registration for EGMD.
 */
public class ModItems {
    private ModItems() { }

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EGMD.MODID);

    public static final DeferredItem<BlockItem> MINING_PORTAL_ITEM = ITEMS.registerSimpleBlockItem("mining_portal", ModBlocks.MINING_PORTAL);
    public static final DeferredItem<BlockItem> NETHER_MINING_PORTAL_ITEM = ITEMS.registerSimpleBlockItem("nether_mining_portal", ModBlocks.NETHER_MINING_PORTAL);
    public static final DeferredItem<BlockItem> END_MINING_PORTAL_ITEM = ITEMS.registerSimpleBlockItem("end_mining_portal", ModBlocks.END_MINING_PORTAL);
    public static final DeferredItem<BlockItem> PEARL_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("pearl_block", ModBlocks.PEARL_BLOCK);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
