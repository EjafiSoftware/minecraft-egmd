package com.ejafi.egmd.core.registration;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.content.block.MiningPortalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles block registration for EGMD.
 */
public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(EGMD.MODID);

    public static final DeferredBlock<Block> PEARL_BLOCK = BLOCKS.register("pearl_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(3.0F, 3.0F)
                    .sound(SoundType.GLASS)
                    .requiresCorrectToolForDrops()));

    public static final Map<PortalVariant, DeferredBlock<MiningPortalBlock>> PORTAL_BLOCKS;

    static {
        Map<PortalVariant, DeferredBlock<MiningPortalBlock>> blocks = new LinkedHashMap<>();
        for (PortalVariant v : PortalVariants.ALL) {
            blocks.put(v, BLOCKS.register(v.id(), () ->
                    new MiningPortalBlock(portalProperties(v.mapColor()), v.dimensionKey(),
                            v.frameBlock(), ModPoiTypes.PORTAL_POI_TYPES.get(v), v.surfaceY())
            ));
        }
        PORTAL_BLOCKS = Collections.unmodifiableMap(blocks);
    }

    private static BlockBehaviour.Properties portalProperties(MapColor color) {
        return BlockBehaviour.Properties.of()
                .mapColor(color).noCollission().strength(-1.0F)
                .sound(SoundType.GLASS).lightLevel(state -> 11)
                .noLootTable().pushReaction(PushReaction.BLOCK);
    }

    private ModBlocks() {}

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
