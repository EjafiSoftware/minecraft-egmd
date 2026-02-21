package com.ejafi.egmd.core.registration;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.content.block.MiningPortalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Handles block registration for EGMD.
 */
public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(EGMD.MODID);

    private ModBlocks() { }

    /**
     * Overworld Mining Portal - Framed with Cobblestone.
     */
    public static final DeferredBlock<MiningPortalBlock> MINING_PORTAL = BLOCKS.register("mining_portal",
            () -> new MiningPortalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .noCollission()
                    .strength(-1.0F)
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 11)
                    .noLootTable()
                    .pushReaction(PushReaction.BLOCK),
                    ModDimensions.OVERWORLD_MINING_LEVEL_KEY,
                    Blocks.COBBLESTONE,
                    ModPoiTypes.MINING_PORTAL,
                    ModDimensions.OVERWORLD_MINING_SURFACE_Y));

    /**
     * Nether Mining Portal - Framed with Netherrack.
     */
    public static final DeferredBlock<MiningPortalBlock> NETHER_MINING_PORTAL = BLOCKS.register("nether_mining_portal",
            () -> new MiningPortalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .noCollission()
                    .strength(-1.0F)
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 11)
                    .noLootTable()
                    .pushReaction(PushReaction.BLOCK),
                    ModDimensions.NETHER_MINING_LEVEL_KEY,
                    Blocks.NETHERRACK,
                    ModPoiTypes.NETHER_MINING_PORTAL,
                    ModDimensions.NETHER_MINING_SURFACE_Y));

    /**
     * End Mining Portal - Framed with End Stone.
     */
    public static final DeferredBlock<MiningPortalBlock> END_MINING_PORTAL = BLOCKS.register("end_mining_portal",
            () -> new MiningPortalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .noCollission()
                    .strength(-1.0F)
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 11)
                    .noLootTable()
                    .pushReaction(PushReaction.BLOCK),
                    ModDimensions.END_MINING_LEVEL_KEY,
                    Blocks.END_STONE,
                    ModPoiTypes.END_MINING_PORTAL,
                    ModDimensions.END_MINING_SURFACE_Y));

    public static final DeferredBlock<Block> PEARL_BLOCK = BLOCKS.register("pearl_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(3.0F, 3.0F)
                    .sound(SoundType.GLASS)
                    .requiresCorrectToolForDrops()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
