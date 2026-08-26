package com.ejafi.egmd.content.block;

import com.ejafi.egmd.EGMD;
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

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Handles the ignition of mining portals when using specific items on frame blocks.
 */
@EventBusSubscriber(modid = EGMD.MODID)
public class PortalIgnitionHandler {
    private static final AtomicReference<Map<Block, MiningPortalBlock>> frameToPortal = new AtomicReference<>(Map.of());
    private static final TagKey<Item> PORTAL_IGNITERS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(EGMD.MODID, "portal_igniters"));

    private PortalIgnitionHandler() {}

    public static void initFrameMap(Map<Block, MiningPortalBlock> map) {
        frameToPortal.set(Map.copyOf(map));
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

        MiningPortalBlock portalBlock = frameToPortal.get().get(state.getBlock());
        if (portalBlock != null) {
            BlockPos portalPos = pos.relative(event.getFace());
            if (level.getBlockState(portalPos).isAir() && trySpawnPortal(level, portalPos, portalBlock, portalBlock.getFrameBlock())) {
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

    private static boolean trySpawnPortal(Level level, BlockPos pos, Block portalBlock, Block frameBlock) {
        return trySpawnPortalInAxis(level, pos, Axis.X, portalBlock, frameBlock) ||
                trySpawnPortalInAxis(level, pos, Axis.Z, portalBlock, frameBlock);
    }

    private static boolean trySpawnPortalInAxis(Level level, BlockPos pos, Axis axis, Block portalBlock, Block frameBlock) {
        MiningPortalShape shape = new MiningPortalShape(level, pos, axis, portalBlock, frameBlock);
        if (shape.isValid()) {
            shape.createPortalBlocks();
            return true;
        }
        return false;
    }
}
