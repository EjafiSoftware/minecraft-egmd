package com.ejafi.egmd.content.block;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.core.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles the ignition of mining portals when using specific items on frame blocks.
 */
@EventBusSubscriber(modid = EGMD.MODID)
public class PortalIgnitionHandler {
    private static volatile Map<Block, MiningPortalBlock> frameToPortal = Map.of();
    private static final TagKey<Item> PORTAL_IGNITERS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(EGMD.MODID, "portal_igniters"));

    private PortalIgnitionHandler() {}

    /**
     * Builds the frame-to-portal lookup. Must be called after block registration has completed.
     */
    public static void init() {
        Map<Block, MiningPortalBlock> map = new HashMap<>();
        for (DeferredBlock<MiningPortalBlock> holder : ModBlocks.PORTAL_BLOCKS.values()) {
            MiningPortalBlock block = holder.get();
            map.put(block.getFrameBlock(), block);
        }
        frameToPortal = Map.copyOf(map);
    }

    /**
     * Listens for right-click events on blocks to trigger portal creation.
     */
    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide())
            return;

        ItemStack stack = event.getItemStack();
        if (!(stack.is(PORTAL_IGNITERS) || stack.canPerformAction(ItemAbilities.FIRESTARTER_LIGHT))) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Player player = event.getEntity();

        MiningPortalBlock portalBlock = frameToPortal.get(state.getBlock());
        if (portalBlock != null) {
            BlockPos portalPos = pos.relative(event.getFace());
            if (level.getBlockState(portalPos).isAir() && trySpawnPortal(level, portalPos, portalBlock)) {
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
                damageIgniter(stack, player);
            }
        }
    }

    private static void damageIgniter(ItemStack stack, Player player) {
        if (!player.getAbilities().instabuild) {
            stack.hurtAndBreak(1, player, player.getEquipmentSlotForItem(stack));
        }
    }

    private static boolean trySpawnPortal(Level level, BlockPos pos, MiningPortalBlock portalBlock) {
        return trySpawnPortalInAxis(level, pos, Axis.X, portalBlock) ||
                trySpawnPortalInAxis(level, pos, Axis.Z, portalBlock);
    }

    private static boolean trySpawnPortalInAxis(Level level, BlockPos pos, Axis axis, MiningPortalBlock portalBlock) {
        MiningPortalShape shape = new MiningPortalShape(level, pos, axis, portalBlock);
        if (shape.isValid()) {
            shape.createPortalBlocks(level);
            return true;
        }
        return false;
    }
}
