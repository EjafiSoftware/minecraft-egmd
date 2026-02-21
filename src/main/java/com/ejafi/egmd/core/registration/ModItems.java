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
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EGMD.MODID);

    public static final DeferredItem<BlockItem> PEARL_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("pearl_block", ModBlocks.PEARL_BLOCK);

    static {
        for (PortalVariant v : PortalVariants.ALL) {
            ITEMS.registerSimpleBlockItem(v.id(), ModBlocks.PORTAL_BLOCKS.get(v));
        }
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
