package com.ejafi.egmd.dimension;

import com.ejafi.egmd.EGMD;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = EGMD.MOD_ID)
public final class BedGuard {
    private BedGuard() {}

    static boolean allowsUse(BlockState state, ResourceKey<Level> dimension) {
        return !(state.getBlock() instanceof BedBlock)
                || MiningDimension.findByLevelKey(dimension).isEmpty();
    }

    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (event.getHand() != InteractionHand.MAIN_HAND
                || allowsUse(level.getBlockState(event.getPos()), level.dimension())
                || bypassesBlockUse(event)) {
            return;
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (!level.isClientSide()) {
            event.getEntity().displayClientMessage(Component.translatable(DimensionMessages.BED_REFUSED), true);
        }
    }

    private static boolean bypassesBlockUse(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        return player.isSecondaryUseActive()
                && !(mainHand.isEmpty() && offHand.isEmpty())
                && !(mainHand.doesSneakBypassUse(level, pos, player) && offHand.doesSneakBypassUse(level, pos, player));
    }
}
