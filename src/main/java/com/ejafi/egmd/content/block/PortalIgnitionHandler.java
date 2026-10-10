package com.ejafi.egmd.content.block;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.core.registration.ModBlocks;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredBlock;

/**
 * Lights mining portals when a player uses an igniter on a frame block.
 *
 * <p>Runs on both sides. The client only checks the frame and cancels its own prediction, so it
 * never shows fire or spends an item the server did not; the server fills the frame and charges
 * the item.</p>
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
        ItemStack stack = event.getItemStack();
        if (!(stack.is(PORTAL_IGNITERS) || stack.canPerformAction(ItemAbilities.FIRESTARTER_LIGHT))) {
            return;
        }

        Level level = event.getLevel();
        Player player = event.getEntity();
        Optional<Ignition> found = findIgnition(level, event.getPos(), event.getFace());
        if (found.isEmpty()) return;
        Ignition ignition = found.get();

        if (player.isSpectator()
                || event.getUseItem() == TriState.FALSE
                || !player.mayUseItemAt(ignition.portalPos(), ignition.face(), stack)
                || MiningPortalBlock.resolveDestination(level.dimension(), ignition.portalBlock().getDimensionKey()) == null) {
            return;
        }

        Optional<MiningPortalShape> shape = findEmptyFrame(level, ignition.portalPos(), ignition.portalBlock());
        if (shape.isEmpty()) return;

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (level.isClientSide()) return;

        shape.get().createPortalBlocks(level);
        boolean fireCharge = stack.getItem() instanceof FireChargeItem;
        level.playSound(null, ignition.portalPos(), fireCharge ? SoundEvents.FIRECHARGE_USE : SoundEvents.FLINTANDSTEEL_USE,
                SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
        level.gameEvent(player, GameEvent.BLOCK_PLACE, ignition.portalPos());
        chargeIgniter(stack, player, event.getHand(), fireCharge);
    }

    /**
     * Where a click would light a portal: the open cell in front of a clicked frame block, or a
     * clicked fire burning on top of a frame block.
     *
     * @param portalPos   the cell the portal would be lit from
     * @param face        the face whose opposite neighbor of {@code portalPos} is the frame block
     * @param portalBlock the portal variant that frame belongs to
     */
    private record Ignition(BlockPos portalPos, Direction face, MiningPortalBlock portalBlock) {}

    private static Optional<Ignition> findIgnition(Level level, BlockPos clickedPos, @Nullable Direction face) {
        BlockState clicked = level.getBlockState(clickedPos);
        if (clicked.getBlock() instanceof BaseFireBlock) {
            MiningPortalBlock portalBlock = findPortalForFrame(level.getBlockState(clickedPos.below()));
            return portalBlock == null ? Optional.empty() : Optional.of(new Ignition(clickedPos, Direction.UP, portalBlock));
        }

        MiningPortalBlock portalBlock = findPortalForFrame(clicked);
        if (portalBlock == null || face == null) return Optional.empty();
        BlockPos portalPos = clickedPos.relative(face);
        BlockState target = level.getBlockState(portalPos);
        if (!target.isAir() && !(target.getBlock() instanceof BaseFireBlock)) return Optional.empty();
        return Optional.of(new Ignition(portalPos, face, portalBlock));
    }

    @Nullable
    private static MiningPortalBlock findPortalForFrame(BlockState frame) {
        Block block = frame.getBlock() instanceof InfestedBlock infested ? infested.getHostBlock() : frame.getBlock();
        return frameToPortal.get(block);
    }

    private static Optional<MiningPortalShape> findEmptyFrame(Level level, BlockPos pos, MiningPortalBlock portalBlock) {
        for (Axis axis : new Axis[] {Axis.X, Axis.Z}) {
            MiningPortalShape shape = new MiningPortalShape(level, pos, axis, portalBlock);
            if (shape.isEmptyFrame()) return Optional.of(shape);
        }
        return Optional.empty();
    }

    private static void chargeIgniter(ItemStack stack, Player player, InteractionHand hand, boolean fireCharge) {
        if (fireCharge) {
            stack.consume(1, player);
        } else {
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        }
    }
}
