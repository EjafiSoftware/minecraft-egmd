package com.ejafi.egmd.portal;

import com.ejafi.egmd.EGMD;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = EGMD.MOD_ID)
public final class PortalIgnitionHandler {
    public static final TagKey<Item> PORTAL_IGNITERS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(EGMD.MOD_ID, "portal_igniters"));
    private static final List<Axis> PORTAL_AXES = List.of(Axis.X, Axis.Z);
    private static final Map<Block, PortalVariant> VARIANTS_BY_FRAME = PortalVariants.ALL.stream()
            .collect(Collectors.toUnmodifiableMap(PortalVariant::frameBlock, Function.identity()));

    private PortalIgnitionHandler() {}

    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!isIgniter(event.getItemStack())) {
            return;
        }

        Level level = event.getLevel();
        Optional<Ignition> found =
                findIgnition(level, event.getHitVec()).filter(ignition -> mayIgnite(event, ignition));
        if (found.isEmpty()) {
            return;
        }

        Ignition ignition = found.orElseThrow();
        Optional<MiningPortalShape> shape = findEmptyFrame(level, ignition);
        if (shape.isEmpty()) {
            return;
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (level.isClientSide()) {
            return;
        }

        shape.orElseThrow().createPortalBlocks(level);
        SoundEvent sound = chargeIgniter(event);
        level.playSound(
                null,
                ignition.portalPos(),
                sound,
                SoundSource.BLOCKS,
                1.0F,
                level.getRandom().nextFloat() * 0.4F + 0.8F);
        level.gameEvent(event.getEntity(), GameEvent.BLOCK_PLACE, ignition.portalPos());
    }

    static boolean isIgniter(ItemStack stack) {
        return stack.is(PORTAL_IGNITERS) || stack.canPerformAction(ItemAbilities.FIRESTARTER_LIGHT);
    }

    private record Ignition(BlockPos portalPos, Direction face, MiningPortalBlock portalBlock) {}

    private static boolean mayIgnite(PlayerInteractEvent.RightClickBlock event, Ignition ignition) {
        Player player = event.getEntity();
        return !player.isSpectator()
                && event.getUseItem() != TriState.FALSE
                && player.mayUseItemAt(ignition.portalPos(), ignition.face(), event.getItemStack())
                && MiningPortalBlock.resolveDestination(
                                event.getLevel().dimension(),
                                ignition.portalBlock().getDimensionKey())
                        != null;
    }

    private static Optional<Ignition> findIgnition(BlockGetter level, BlockHitResult hit) {
        BlockPos clickedPos = hit.getBlockPos();
        BlockState clicked = level.getBlockState(clickedPos);
        if (clicked.getBlock() instanceof BaseFireBlock) {
            return findPortalForFrame(level.getBlockState(clickedPos.below()))
                    .map(portalBlock -> new Ignition(clickedPos, Direction.UP, portalBlock));
        }

        Direction face = hit.getDirection();
        BlockPos portalPos = clickedPos.relative(face);
        return findPortalForFrame(clicked)
                .filter(portalBlock -> MiningPortalShape.isOpenCell(level.getBlockState(portalPos)))
                .map(portalBlock -> new Ignition(portalPos, face, portalBlock));
    }

    private static Optional<MiningPortalBlock> findPortalForFrame(BlockState frame) {
        return Optional.ofNullable(VARIANTS_BY_FRAME.get(MiningPortalShape.resolveFrameHost(frame)))
                .map(variant -> PortalRegisters.getPortalBlock(variant).get());
    }

    private static Optional<MiningPortalShape> findEmptyFrame(BlockGetter level, Ignition ignition) {
        return PORTAL_AXES.stream()
                .map(axis -> new MiningPortalShape(level, ignition.portalPos(), axis, ignition.portalBlock()))
                .filter(MiningPortalShape::isEmptyFrame)
                .findFirst();
    }

    private static SoundEvent chargeIgniter(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        Player player = event.getEntity();
        if (stack.getItem() instanceof FireChargeItem) {
            stack.consume(1, player);
            return SoundEvents.FIRECHARGE_USE;
        }

        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(event.getHand()));
        return SoundEvents.FLINTANDSTEEL_USE;
    }
}
