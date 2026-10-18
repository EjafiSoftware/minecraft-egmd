package com.ejafi.egmd.dimension;

import com.ejafi.egmd.Egmd;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

@EventBusSubscriber(modid = Egmd.MOD_ID)
public final class BedGuard {
    private BedGuard() {}

    static boolean allowsUse(BlockState state, Set<Restriction> restrictions) {
        return !(state.getBlock() instanceof BedBlock) || !restrictions.contains(Restriction.NO_BEDS);
    }

    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (event.getHand() != InteractionHand.MAIN_HAND
                || allowsUse(level.getBlockState(event.getPos()), EgmdDimensions.getRestrictions(level.dimension()))
                || bypassesBlockUse(event.getEntity(), event.getPos())) {
            return;
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (!level.isClientSide()) {
            event.getEntity().displayClientMessage(Component.translatable(DimensionMessages.BED_REFUSED), true);
        }
    }

    static boolean bypassesBlockUse(Player player, BlockPos pos) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        Level level = player.level();
        return player.isSecondaryUseActive()
                && !(mainHand.isEmpty() && offHand.isEmpty())
                && !(mainHand.doesSneakBypassUse(level, pos, player) && offHand.doesSneakBypassUse(level, pos, player));
    }
}
