package com.ejafi.egmd.content.block;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.core.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Handles the ignition of mining portals when using specific items on frame blocks.
 */
@EventBusSubscriber(modid = EGMD.MODID)
public class PortalIgnitionHandler {
    private PortalIgnitionHandler() { }

    /**
     * Listens for right-click events on blocks to trigger portal creation.
     */
    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        ItemStack stack = event.getItemStack();
        Player player = event.getEntity();

        // Check if player is holding flint and steel
        if (stack.is(Items.FLINT_AND_STEEL)) {
            BlockState state = level.getBlockState(pos);
            
            // Search for a matching portal by frame block
            for (var blockEntry : ModBlocks.BLOCKS.getEntries()) {
                if (blockEntry.get() instanceof MiningPortalBlock portalBlock) {
                    if (state.is(portalBlock.getFrameBlock())) {
                        BlockPos portalPos = pos.relative(event.getFace());
                        if (level.getBlockState(portalPos).isAir() && trySpawnPortal(level, portalPos, portalBlock, portalBlock.getFrameBlock())) {
                            event.setCancellationResult(InteractionResult.SUCCESS);
                            event.setCanceled(true);
                            damageIgniter(stack, player);
                            return;
                        }
                    }
                }
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
