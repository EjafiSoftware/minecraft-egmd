package com.ejafi.egmd.content.block;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.core.registration.ModBlocks;
import com.ejafi.egmd.core.registration.PortalVariants;
import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * In-world integration tests for the portal machinery that unit tests cannot cover.
 */
@GameTestHolder(EGMD.MODID)
public class PortalGameTests {
    private static final String TEMPLATE = "empty_7x7x7";
    private static final BlockPos ORIGIN = new BlockPos(2, 2, 2);

    private PortalGameTests() {}

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void build_places_portal_blocks_east_of_origin_when_axis_is_x(GameTestHelper helper) {
        MiningPortalBlock portal = getOverworldPortal();

        PortalStructureBuilder.build(helper.getLevel(), helper.absolutePos(ORIGIN), Axis.X, portal, false);

        BlockState expected = portal.defaultBlockState().setValue(MiningPortalBlock.AXIS, Axis.X);
        assertStatePresent(helper, expected, ORIGIN);
        assertStatePresent(helper, expected, ORIGIN.east());
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void build_surrounds_portal_with_frame_when_axis_is_x(GameTestHelper helper) {
        MiningPortalBlock portal = getOverworldPortal();

        PortalStructureBuilder.build(helper.getLevel(), helper.absolutePos(ORIGIN), Axis.X, portal, false);

        for (BlockPos frame : new BlockPos[] {ORIGIN.west(), ORIGIN.east(2), ORIGIN.above(MiningPortalShape.MIN_HEIGHT), ORIGIN.below()}) {
            helper.assertBlockPresent(portal.getFrameBlock(), frame);
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void build_places_portal_blocks_south_of_origin_when_axis_is_z(GameTestHelper helper) {
        MiningPortalBlock portal = getOverworldPortal();

        PortalStructureBuilder.build(helper.getLevel(), helper.absolutePos(ORIGIN), Axis.Z, portal, false);

        BlockState expected = portal.defaultBlockState().setValue(MiningPortalBlock.AXIS, Axis.Z);
        assertStatePresent(helper, expected, ORIGIN);
        assertStatePresent(helper, expected, ORIGIN.south());
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void build_surrounds_portal_with_frame_when_axis_is_z(GameTestHelper helper) {
        MiningPortalBlock portal = getOverworldPortal();

        PortalStructureBuilder.build(helper.getLevel(), helper.absolutePos(ORIGIN), Axis.Z, portal, false);

        for (BlockPos frame : new BlockPos[] {ORIGIN.north(), ORIGIN.south(2), ORIGIN.above(MiningPortalShape.MIN_HEIGHT), ORIGIN.below()}) {
            helper.assertBlockPresent(portal.getFrameBlock(), frame);
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void build_clears_exit_space_when_site_is_forced(GameTestHelper helper) {
        for (BlockPos exit : PortalFootprint.listExitCells(ORIGIN, Axis.X)) {
            helper.setBlock(exit, Blocks.DIRT);
        }

        PortalStructureBuilder.build(helper.getLevel(), helper.absolutePos(ORIGIN), Axis.X, getOverworldPortal(), true);

        for (BlockPos exit : PortalFootprint.listExitCells(ORIGIN, Axis.X)) {
            helper.assertBlockPresent(Blocks.AIR, exit);
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void find_portal_around_returns_portal_when_it_sits_far_above_target(GameTestHelper helper) {
        MiningPortalBlock portal = getOverworldPortal();
        BlockPos portalPos = new BlockPos(3, 5, 3);
        helper.setBlock(portalPos, portal.defaultBlockState());
        ServerLevel level = helper.getLevel();

        Optional<BlockPos> found = portal.findPortalAround(level, helper.absolutePos(new BlockPos(3, 0, 3)), level.getWorldBorder(), 2);

        if (!Optional.of(helper.absolutePos(portalPos)).equals(found)) {
            helper.fail("Expected the portal at " + helper.absolutePos(portalPos) + " but found " + found);
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void select_exit_returns_back_side_when_front_is_blocked(GameTestHelper helper) {
        PortalStructureBuilder.build(helper.getLevel(), helper.absolutePos(ORIGIN), Axis.X, getOverworldPortal(), false);
        for (BlockPos exit : PortalFootprint.listExitCells(ORIGIN, Axis.X)) {
            helper.setBlock(exit, Blocks.STONE);
        }
        BlockPos absoluteOrigin = helper.absolutePos(ORIGIN);

        PortalExitPlacement exit = MiningPortalBlock.selectExit(helper.getLevel(), minimumPortalAt(absoluteOrigin), Axis.X,
                helper.makeMockPlayer(GameType.SURVIVAL));

        PortalExitPlacement expected = PortalExitPlacement.computeBack(absoluteOrigin, Axis.X, MiningPortalShape.MIN_WIDTH);
        if (!expected.equals(exit)) {
            helper.fail("Expected " + expected + " but was " + exit);
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void select_exit_returns_inside_portal_when_both_sides_are_blocked(GameTestHelper helper) {
        PortalStructureBuilder.build(helper.getLevel(), helper.absolutePos(ORIGIN), Axis.X, getOverworldPortal(), false);
        for (BlockPos exit : PortalFootprint.listExitCells(ORIGIN, Axis.X)) {
            helper.setBlock(exit, Blocks.STONE);
            helper.setBlock(exit.north(2), Blocks.STONE);
        }
        BlockPos absoluteOrigin = helper.absolutePos(ORIGIN);

        PortalExitPlacement exit = MiningPortalBlock.selectExit(helper.getLevel(), minimumPortalAt(absoluteOrigin), Axis.X,
                helper.makeMockPlayer(GameType.SURVIVAL));

        PortalExitPlacement inside = PortalExitPlacement.computeInside(absoluteOrigin, Axis.X, MiningPortalShape.MIN_WIDTH);
        if (exit.position().distanceTo(inside.position()) > 0.5) {
            helper.fail("Expected an arrival inside the portal near " + inside + " but was " + exit);
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void can_place_liquid_keeps_portal_intact_when_water_flows_against_it(GameTestHelper helper) {
        MiningPortalBlock portal = getOverworldPortal();
        PortalStructureBuilder.build(helper.getLevel(), helper.absolutePos(ORIGIN), Axis.X, portal, false);

        helper.setBlock(ORIGIN.south(), Blocks.WATER);

        helper.runAfterDelay(40, () -> {
            for (int x = 0; x < MiningPortalShape.MIN_WIDTH; x++) {
                for (int y = 0; y < MiningPortalShape.MIN_HEIGHT; y++) {
                    helper.assertBlockPresent(portal, ORIGIN.east(x).above(y));
                }
            }
            helper.succeed();
        });
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void search_returns_site_clear_of_container_when_container_sits_on_target_column(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos center = helper.absolutePos(new BlockPos(3, 0, 3));
        int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, center.getX(), center.getZ());
        level.setBlockAndUpdate(new BlockPos(center.getX(), groundY, center.getZ()), Blocks.CHEST.defaultBlockState());

        Optional<BlockPos> result = new SpiralSearch().search(level, center, Axis.X);

        if (result.isEmpty() || !PortalFootprint.canHost(level, level.getWorldBorder(), result.get(), Axis.X)) {
            helper.fail("Expected a site that can host a portal but was " + result);
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void on_right_click_block_fills_frame_with_portal_blocks_when_frame_is_valid(GameTestHelper helper) {
        MiningPortalBlock portal = getOverworldPortal();
        buildFrame(helper, portal);

        clickFrame(helper, makeSurvivalPlayerHolding(helper, new ItemStack(Items.FLINT_AND_STEEL)), InteractionHand.MAIN_HAND);

        assertFrameIsFilled(helper, portal);
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void on_right_click_block_leaves_frame_empty_when_player_is_in_adventure_mode(GameTestHelper helper) {
        buildFrame(helper, getOverworldPortal());

        clickFrame(helper, makePlayerHolding(helper, GameType.ADVENTURE, new ItemStack(Items.FLINT_AND_STEEL)), InteractionHand.MAIN_HAND);

        assertFrameIsEmpty(helper);
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void on_right_click_block_leaves_vanilla_item_use_alone_when_player_is_in_adventure_mode(GameTestHelper helper) {
        buildFrame(helper, getOverworldPortal());

        PlayerInteractEvent.RightClickBlock event = clickFrame(helper,
                makePlayerHolding(helper, GameType.ADVENTURE, new ItemStack(Items.FLINT_AND_STEEL)), InteractionHand.MAIN_HAND);

        if (event.isCanceled()) {
            helper.fail("The refused ignition still canceled vanilla item use");
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void on_right_click_block_leaves_frame_empty_when_player_is_spectator(GameTestHelper helper) {
        buildFrame(helper, getOverworldPortal());

        clickFrame(helper, makePlayerHolding(helper, GameType.SPECTATOR, new ItemStack(Items.FLINT_AND_STEEL)), InteractionHand.MAIN_HAND);

        assertFrameIsEmpty(helper);
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void on_right_click_block_leaves_frame_empty_when_item_use_is_denied(GameTestHelper helper) {
        buildFrame(helper, getOverworldPortal());
        Player player = makeSurvivalPlayerHolding(helper, new ItemStack(Items.FLINT_AND_STEEL));
        PlayerInteractEvent.RightClickBlock event = createClick(helper, player, InteractionHand.MAIN_HAND);
        event.setUseItem(TriState.FALSE);

        NeoForge.EVENT_BUS.post(event);

        assertFrameIsEmpty(helper);
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void on_right_click_block_fills_frame_when_fire_burns_inside(GameTestHelper helper) {
        MiningPortalBlock portal = getOverworldPortal();
        buildFrame(helper, portal);
        helper.setBlock(new BlockPos(3, 2, 2), Blocks.FIRE);

        clickFrame(helper, makeSurvivalPlayerHolding(helper, new ItemStack(Items.FLINT_AND_STEEL)), InteractionHand.MAIN_HAND);

        assertFrameIsFilled(helper, portal);
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void on_right_click_block_keeps_existing_portal_axis_when_frame_already_holds_portal_blocks(GameTestHelper helper) {
        MiningPortalBlock portal = getOverworldPortal();
        buildFrame(helper, portal);
        helper.setBlock(new BlockPos(2, 4, 2), portal.defaultBlockState().setValue(MiningPortalBlock.AXIS, Axis.Z));

        clickFrame(helper, makeSurvivalPlayerHolding(helper, new ItemStack(Items.FLINT_AND_STEEL)), InteractionHand.MAIN_HAND);

        helper.assertBlockProperty(new BlockPos(2, 4, 2), MiningPortalBlock.AXIS, Axis.Z);
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void on_right_click_block_damages_off_hand_igniter_when_igniter_is_in_off_hand(GameTestHelper helper) {
        buildFrame(helper, getOverworldPortal());
        Player player = makeSurvivalPlayerHolding(helper, new ItemStack(Items.DIAMOND_PICKAXE));
        ItemStack igniter = new ItemStack(Items.FLINT_AND_STEEL);
        player.setItemInHand(InteractionHand.OFF_HAND, igniter);

        clickFrame(helper, player, InteractionHand.OFF_HAND);

        if (igniter.getDamageValue() != 1) {
            helper.fail("Expected the off-hand igniter to take 1 damage but it took " + igniter.getDamageValue());
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void on_right_click_block_spares_main_hand_item_when_igniter_is_in_off_hand(GameTestHelper helper) {
        buildFrame(helper, getOverworldPortal());
        ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        Player player = makeSurvivalPlayerHolding(helper, pickaxe);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.FLINT_AND_STEEL));

        clickFrame(helper, player, InteractionHand.OFF_HAND);

        if (pickaxe.getDamageValue() != 0) {
            helper.fail("The main-hand pickaxe took " + pickaxe.getDamageValue() + " damage");
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void on_right_click_block_keeps_igniter_when_igniter_is_unbreakable(GameTestHelper helper) {
        buildFrame(helper, getOverworldPortal());
        ItemStack igniter = new ItemStack(Items.FLINT_AND_STEEL);
        igniter.set(DataComponents.UNBREAKABLE, new Unbreakable(true));

        clickFrame(helper, makeSurvivalPlayerHolding(helper, igniter), InteractionHand.MAIN_HAND);

        if (igniter.getCount() != 1) {
            helper.fail("The unbreakable igniter was consumed; " + igniter.getCount() + " left");
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void on_right_click_block_consumes_one_fire_charge_when_fire_charge_lights_portal(GameTestHelper helper) {
        buildFrame(helper, getOverworldPortal());
        ItemStack fireCharges = new ItemStack(Items.FIRE_CHARGE, 2);

        clickFrame(helper, makeSurvivalPlayerHolding(helper, fireCharges), InteractionHand.MAIN_HAND);

        if (fireCharges.getCount() != 1) {
            helper.fail("Expected 1 fire charge left but there were " + fireCharges.getCount());
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void get_portal_transition_time_matches_vanilla_delay_when_player_is_in_survival(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        int expected = helper.getLevel().getGameRules().getInt(GameRules.RULE_PLAYERS_NETHER_PORTAL_DEFAULT_DELAY);

        int actual = getOverworldPortal().getPortalTransitionTime(helper.getLevel(), player);

        if (actual != expected) {
            helper.fail("Expected transition time " + expected + " but was " + actual);
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void process_portal_teleportation_waits_when_vanilla_delay_has_not_elapsed(GameTestHelper helper) {
        MiningPortalBlock portal = getOverworldPortal();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pos = helper.absolutePos(ORIGIN);
        int delay = helper.getLevel().getGameRules().getInt(GameRules.RULE_PLAYERS_NETHER_PORTAL_DEFAULT_DELAY);

        for (int tick = 0; tick < delay; tick++) {
            if (stepIntoPortal(player, portal, pos).processPortalTeleportation(helper.getLevel(), player, true)) {
                helper.fail("Ready to teleport after only " + tick + " ticks inside the portal");
            }
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void process_portal_teleportation_is_ready_when_vanilla_delay_has_elapsed(GameTestHelper helper) {
        MiningPortalBlock portal = getOverworldPortal();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pos = helper.absolutePos(ORIGIN);
        int delay = helper.getLevel().getGameRules().getInt(GameRules.RULE_PLAYERS_NETHER_PORTAL_DEFAULT_DELAY);

        for (int tick = 0; tick < delay; tick++) {
            stepIntoPortal(player, portal, pos).processPortalTeleportation(helper.getLevel(), player, true);
        }

        if (!stepIntoPortal(player, portal, pos).processPortalTeleportation(helper.getLevel(), player, true)) {
            helper.fail("Not ready to teleport after the vanilla delay elapsed");
        }
        helper.succeed();
    }

    private static PortalProcessor stepIntoPortal(Player player, MiningPortalBlock portal, BlockPos pos) {
        player.setAsInsidePortal(portal, pos);
        PortalProcessor process = player.portalProcess;
        if (process == null) {
            throw new GameTestAssertException("Stepping into the portal did not start a portal process");
        }
        return process;
    }

    private static Player makeSurvivalPlayerHolding(GameTestHelper helper, ItemStack mainHand) {
        return makePlayerHolding(helper, GameType.SURVIVAL, mainHand);
    }

    private static Player makePlayerHolding(GameTestHelper helper, GameType gameType, ItemStack mainHand) {
        Player player = helper.makeMockPlayer(gameType);
        gameType.updatePlayerAbilities(player.getAbilities());
        player.setItemInHand(InteractionHand.MAIN_HAND, mainHand);
        return player;
    }

    private static PlayerInteractEvent.RightClickBlock createClick(GameTestHelper helper, Player player, InteractionHand hand) {
        BlockPos clickedFrame = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(clickedFrame), Direction.UP, clickedFrame, false);
        return new PlayerInteractEvent.RightClickBlock(player, hand, clickedFrame, hit);
    }

    private static PlayerInteractEvent.RightClickBlock clickFrame(GameTestHelper helper, Player player, InteractionHand hand) {
        return NeoForge.EVENT_BUS.post(createClick(helper, player, hand));
    }

    private static BlockUtil.FoundRectangle minimumPortalAt(BlockPos absoluteOrigin) {
        return new BlockUtil.FoundRectangle(absoluteOrigin, MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT);
    }

    private static void assertStatePresent(GameTestHelper helper, BlockState expected, BlockPos relativePos) {
        BlockState actual = helper.getBlockState(relativePos);
        if (actual != expected) {
            helper.fail("Expected " + expected + " but was " + actual + " at " + relativePos);
        }
    }

    private static void assertFrameIsFilled(GameTestHelper helper, MiningPortalBlock portal) {
        for (int x = 2; x <= 3; x++) {
            for (int y = 2; y <= 4; y++) {
                helper.assertBlockPresent(portal, new BlockPos(x, y, 2));
            }
        }
    }

    private static void assertFrameIsEmpty(GameTestHelper helper) {
        for (int x = 2; x <= 3; x++) {
            for (int y = 2; y <= 4; y++) {
                helper.assertBlockPresent(Blocks.AIR, new BlockPos(x, y, 2));
            }
        }
    }

    private static void buildFrame(GameTestHelper helper, MiningPortalBlock portal) {
        for (int x = 1; x <= 4; x++) {
            helper.setBlock(new BlockPos(x, 1, 2), portal.getFrameBlock());
            helper.setBlock(new BlockPos(x, 5, 2), portal.getFrameBlock());
        }
        for (int y = 2; y <= 4; y++) {
            helper.setBlock(new BlockPos(1, y, 2), portal.getFrameBlock());
            helper.setBlock(new BlockPos(4, y, 2), portal.getFrameBlock());
        }
    }

    private static MiningPortalBlock getOverworldPortal() {
        return ModBlocks.PORTAL_BLOCKS.get(PortalVariants.OVERWORLD).get();
    }
}
