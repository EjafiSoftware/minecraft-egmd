package com.ejafi.egmd.pearl;

import com.ejafi.egmd.EGMD;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PearlBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(EGMD.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EGMD.MOD_ID);

    public static final DeferredBlock<Block> PEARL_BLOCK = BLOCKS.register(
            "pearl_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(3.0F, 3.0F)
                    .sound(SoundType.GLASS)
                    .requiresCorrectToolForDrops()));

    private static final DeferredItem<BlockItem> PEARL_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(PEARL_BLOCK);

    private PearlBlocks() {}

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        modEventBus.addListener(PearlBlocks::addToCreativeTab);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.NATURAL_BLOCKS)) {
            event.accept(PEARL_BLOCK_ITEM);
        }
    }
}
