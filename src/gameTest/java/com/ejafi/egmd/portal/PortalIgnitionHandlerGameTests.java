package com.ejafi.egmd.portal;

import static com.ejafi.egmd.portal.PortalGameTestSupport.ORIGIN;

import com.ejafi.egmd.Egmd;
import com.ejafi.egmd.GameTestTemplates;
import java.util.Map;
import java.util.Objects;
import net.datafaker.Faker;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Egmd.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PortalIgnitionHandlerGameTests {
    private static final Faker FAKER = new Faker();
    private static final int ELSEWHERE_ORIGIN_Y = 64;

    private PortalIgnitionHandlerGameTests() {}

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_fills_frame_when_corners_are_default_corner_blocks(GameTestHelper helper) {
        PortalVariant variant = pickVariant();
        createFrameBuilder(helper, variant)
                .withCornerBlock(variant.defaultCornerBlock())
                .build(PortalGameTestSupport.locateFrameAtOrigin(helper, Axis.X));

        clickFrame(helper, makePlayerHoldingFlintAndSteel(helper, GameType.SURVIVAL));

        PortalGameTestSupport.assertInteriorHolds(helper, PortalGameTestSupport.getPortalState(variant, Axis.X));
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_leaves_frame_empty_when_corners_are_frame_blocks(GameTestHelper helper) {
        createFrameBuilder(helper, pickVariant()).build(PortalGameTestSupport.locateFrameAtOrigin(helper, Axis.X));

        clickFrame(helper, makePlayerHoldingFlintAndSteel(helper, GameType.SURVIVAL));

        PortalGameTestSupport.assertInteriorHolds(helper, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_leaves_vanilla_item_use_alone_when_corners_are_frame_blocks(
            GameTestHelper helper) {
        createFrameBuilder(helper, pickVariant()).build(PortalGameTestSupport.locateFrameAtOrigin(helper, Axis.X));

        PlayerInteractEvent.RightClickBlock event =
                clickFrame(helper, makePlayerHoldingFlintAndSteel(helper, GameType.SURVIVAL));

        helper.assertValueEqual(event.isCanceled(), false, "item use canceled");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_fills_frame_when_corners_are_four_different_corner_blocks(
            GameTestHelper helper) {
        createFrameBuilder(helper, PortalVariants.NETHER_MINING)
                .withCorners(Map.of(
                        PortalCorner.LOW_BOTTOM, Blocks.COBBLESTONE.defaultBlockState(),
                        PortalCorner.HIGH_BOTTOM, Blocks.STONE.defaultBlockState(),
                        PortalCorner.LOW_TOP, Blocks.MOSSY_COBBLESTONE.defaultBlockState(),
                        PortalCorner.HIGH_TOP, Blocks.GRANITE.defaultBlockState()))
                .build(PortalGameTestSupport.locateFrameAtOrigin(helper, Axis.X));

        clickFrame(helper, makePlayerHoldingFlintAndSteel(helper, GameType.SURVIVAL));

        PortalGameTestSupport.assertInteriorHolds(
                helper, PortalGameTestSupport.getPortalState(PortalVariants.NETHER_MINING, Axis.X));
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_fills_frame_when_corners_are_infested_corner_blocks(GameTestHelper helper) {
        createFrameBuilder(helper, PortalVariants.NETHER_MINING)
                .withCornerBlock(Blocks.INFESTED_STONE)
                .build(PortalGameTestSupport.locateFrameAtOrigin(helper, Axis.X));

        clickFrame(helper, makePlayerHoldingFlintAndSteel(helper, GameType.SURVIVAL));

        PortalGameTestSupport.assertInteriorHolds(
                helper, PortalGameTestSupport.getPortalState(PortalVariants.NETHER_MINING, Axis.X));
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_leaves_frame_empty_when_player_is_in_adventure_mode(GameTestHelper helper) {
        buildFrameAroundOrigin(helper);

        clickFrame(helper, makePlayerHoldingFlintAndSteel(helper, GameType.ADVENTURE));

        PortalGameTestSupport.assertInteriorHolds(helper, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_leaves_vanilla_item_use_alone_when_player_is_in_adventure_mode(
            GameTestHelper helper) {
        buildFrameAroundOrigin(helper);

        PlayerInteractEvent.RightClickBlock event =
                clickFrame(helper, makePlayerHoldingFlintAndSteel(helper, GameType.ADVENTURE));

        helper.assertValueEqual(event.isCanceled(), false, "item use canceled");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_leaves_frame_empty_when_player_is_spectator(GameTestHelper helper) {
        buildFrameAroundOrigin(helper);

        clickFrame(helper, makePlayerHoldingFlintAndSteel(helper, GameType.SPECTATOR));

        PortalGameTestSupport.assertInteriorHolds(helper, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_leaves_frame_empty_when_item_use_is_denied(GameTestHelper helper) {
        buildFrameAroundOrigin(helper);
        PlayerInteractEvent.RightClickBlock event =
                createClick(helper, makePlayerHoldingFlintAndSteel(helper, GameType.SURVIVAL));
        event.setUseItem(TriState.FALSE);

        NeoForge.EVENT_BUS.post(event);

        PortalGameTestSupport.assertInteriorHolds(helper, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_fills_frame_when_fire_burns_inside(GameTestHelper helper) {
        buildFrameAroundOrigin(helper);
        helper.setBlock(ORIGIN.east(), Blocks.FIRE);

        clickFrame(helper, makePlayerHoldingFlintAndSteel(helper, GameType.SURVIVAL));

        PortalGameTestSupport.assertPortalFillsInterior(helper, Axis.X);
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_keeps_existing_portal_axis_when_frame_already_holds_portal_blocks(
            GameTestHelper helper) {
        buildFrameAroundOrigin(helper);
        BlockPos topInterior = ORIGIN.above(PortalFrame.MIN_HEIGHT - 1);
        helper.setBlock(topInterior, PortalGameTestSupport.getPortalState(Axis.Z));

        clickFrame(helper, makePlayerHoldingFlintAndSteel(helper, GameType.SURVIVAL));

        helper.assertBlockProperty(topInterior, DimensionPortalBlock.AXIS, Axis.Z);
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_damages_off_hand_igniter_when_igniter_is_in_off_hand(
            GameTestHelper helper) {
        buildFrameAroundOrigin(helper);
        Player player = makeSurvivalPlayerHolding(helper, new ItemStack(Items.DIAMOND_PICKAXE));
        ItemStack igniter = new ItemStack(Items.FLINT_AND_STEEL);
        player.setItemInHand(InteractionHand.OFF_HAND, igniter);

        clickFrameWithOffHand(helper, player);

        helper.assertValueEqual(igniter.getDamageValue(), 1, "off-hand igniter damage");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_spares_main_hand_item_when_igniter_is_in_off_hand(GameTestHelper helper) {
        buildFrameAroundOrigin(helper);
        ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        Player player = makeSurvivalPlayerHolding(helper, pickaxe);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.FLINT_AND_STEEL));

        clickFrameWithOffHand(helper, player);

        helper.assertValueEqual(pickaxe.getDamageValue(), 0, "main-hand pickaxe damage");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_keeps_igniter_when_igniter_is_unbreakable(GameTestHelper helper) {
        buildFrameAroundOrigin(helper);
        ItemStack igniter = new ItemStack(Items.FLINT_AND_STEEL);
        igniter.setDamageValue(igniter.getMaxDamage() - 1);
        igniter.set(DataComponents.UNBREAKABLE, new Unbreakable(true));

        clickFrame(helper, makeSurvivalPlayerHolding(helper, igniter));

        helper.assertValueEqual(igniter.getCount(), 1, "unbreakable igniter count");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_consumes_one_fire_charge_when_fire_charge_lights_portal(
            GameTestHelper helper) {
        buildFrameAroundOrigin(helper);
        int stackSize = FAKER.number().numberBetween(1, Items.FIRE_CHARGE.getDefaultMaxStackSize() + 1);
        ItemStack fireCharges = new ItemStack(Items.FIRE_CHARGE, stackSize);

        clickFrame(helper, makeSurvivalPlayerHolding(helper, fireCharges));

        helper.assertValueEqual(fireCharges.getCount(), stackSize - 1, "fire charges left");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_fills_frame_when_click_hits_fire_standing_on_frame(GameTestHelper helper) {
        buildFrameAroundOrigin(helper);
        helper.setBlock(ORIGIN, Blocks.FIRE);
        BlockPos fire = helper.absolutePos(ORIGIN);

        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(
                makePlayerHoldingFlintAndSteel(helper, GameType.SURVIVAL),
                InteractionHand.MAIN_HAND,
                fire,
                new BlockHitResult(Vec3.atCenterOf(fire), FAKER.options().option(Direction.class), fire, false)));

        PortalGameTestSupport.assertPortalFillsInterior(helper, Axis.X);
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_fills_frame_when_axis_is_z(GameTestHelper helper) {
        createFrameBuilder(helper, PortalVariants.OVERWORLD_MINING)
                .withCornerBlock(PortalVariants.OVERWORLD_MINING.defaultCornerBlock())
                .build(PortalGameTestSupport.locateFrameAtOrigin(helper, Axis.Z));

        clickFrame(helper, makePlayerHoldingFlintAndSteel(helper, GameType.SURVIVAL));

        PortalGameTestSupport.assertPortalFillsInterior(helper, Axis.Z);
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void choose_ignition_returns_missing_corners_when_corners_are_frame_blocks(GameTestHelper helper) {
        PortalVariant variant = pickVariant();
        createFrameBuilder(helper, variant).build(PortalGameTestSupport.locateFrameAtOrigin(helper, Axis.X));

        PortalIgnitionHandler.Ignition ignition = PortalIgnitionHandler.chooseIgnition(
                createClick(helper, makePlayerHoldingFlintAndSteel(helper, GameType.SURVIVAL)));

        helper.assertValueEqual(ignition, new PortalIgnitionHandler.Ignition.MissingCorners(variant), "ignition");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void choose_ignition_returns_ignored_when_frame_stands_in_unlinked_dimension(GameTestHelper helper) {
        ServerLevel elsewhere = Objects.requireNonNull(
                helper.getLevel().getServer().getLevel(FAKER.options().option(Level.NETHER, Level.END)));
        PortalVariant variant = pickVariant();
        PortalFrame frame = new PortalFrame(
                new BlockUtil.FoundRectangle(
                        helper.absolutePos(ORIGIN).atY(ELSEWHERE_ORIGIN_Y),
                        PortalFrame.MIN_WIDTH,
                        PortalFrame.MIN_HEIGHT),
                Axis.X);
        new PortalFrameBuilder(elsewhere, variant.frameBlock())
                .withCornerBlock(variant.defaultCornerBlock())
                .build(frame);
        frame.streamInterior().forEach(pos -> elsewhere.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState()));
        Player player = holdInMainHand(FakePlayerFactory.getMinecraft(elsewhere), new ItemStack(Items.FLINT_AND_STEEL));
        BlockPos clickedFrame = frame.interior().minCorner.below();

        PortalIgnitionHandler.Ignition ignition =
                PortalIgnitionHandler.chooseIgnition(new PlayerInteractEvent.RightClickBlock(
                        player,
                        InteractionHand.MAIN_HAND,
                        clickedFrame,
                        new BlockHitResult(Vec3.atCenterOf(clickedFrame), Direction.UP, clickedFrame, false)));

        helper.assertValueEqual(ignition, new PortalIgnitionHandler.Ignition.Ignored(), "ignition");
        helper.succeed();
    }

    private static PortalVariant pickVariant() {
        return FAKER.options().nextElement(PortalVariants.ALL);
    }

    private static Player makePlayer(GameTestHelper helper, GameType gameType) {
        Player player = helper.makeMockPlayer(gameType);
        gameType.updatePlayerAbilities(player.getAbilities());
        return player;
    }

    private static Player makeSurvivalPlayerHolding(GameTestHelper helper, ItemStack mainHand) {
        return holdInMainHand(makePlayer(helper, GameType.SURVIVAL), mainHand);
    }

    private static Player makePlayerHoldingFlintAndSteel(GameTestHelper helper, GameType gameType) {
        return holdInMainHand(makePlayer(helper, gameType), new ItemStack(Items.FLINT_AND_STEEL));
    }

    private static Player holdInMainHand(Player player, ItemStack stack) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return player;
    }

    private static BlockHitResult aimAtFrame(GameTestHelper helper) {
        BlockPos clickedFrame = helper.absolutePos(ORIGIN.below());
        return new BlockHitResult(Vec3.atCenterOf(clickedFrame), Direction.UP, clickedFrame, false);
    }

    private static PlayerInteractEvent.RightClickBlock createClick(GameTestHelper helper, Player player) {
        BlockHitResult hit = aimAtFrame(helper);
        return new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, hit.getBlockPos(), hit);
    }

    private static PlayerInteractEvent.RightClickBlock clickFrame(GameTestHelper helper, Player player) {
        return NeoForge.EVENT_BUS.post(createClick(helper, player));
    }

    private static void clickFrameWithOffHand(GameTestHelper helper, Player player) {
        BlockHitResult hit = aimAtFrame(helper);
        NeoForge.EVENT_BUS.post(
                new PlayerInteractEvent.RightClickBlock(player, InteractionHand.OFF_HAND, hit.getBlockPos(), hit));
    }

    private static PortalFrameBuilder createFrameBuilder(GameTestHelper helper, PortalVariant variant) {
        return new PortalFrameBuilder(helper.getLevel(), variant.frameBlock());
    }

    private static void buildFrameAroundOrigin(GameTestHelper helper) {
        createFrameBuilder(helper, PortalVariants.OVERWORLD_MINING)
                .withCornerBlock(PortalVariants.OVERWORLD_MINING.defaultCornerBlock())
                .build(PortalGameTestSupport.locateFrameAtOrigin(helper, Axis.X));
    }
}
