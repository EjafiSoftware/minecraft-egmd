package com.ejafi.egmd.portal;

import com.ejafi.egmd.Egmd;
import com.ejafi.egmd.GameTestTemplates;
import com.ejafi.egmd.dimension.EgmdDimensionFaker;
import com.ejafi.egmd.dimension.Restriction;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.datafaker.Faker;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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

@GameTestHolder(Egmd.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PortalGameTests {
    private static final Faker FAKER = new Faker();
    private static final BlockPos ORIGIN = new BlockPos(2, 2, 2);
    private static final BlockPos FLOOR_CENTER = new BlockPos(3, 0, 3);
    private static final BlockPos SPAWN = FLOOR_CENTER.above();
    private static final int EXIT_HEADROOM = 2;
    private static final int WATER_FLOW_TICKS = 40;
    private static final List<BlockPos> X_INTERIOR =
            listCellsBetween(ORIGIN, ORIGIN.offset(PortalFrame.MIN_WIDTH - 1, PortalFrame.MIN_HEIGHT - 1, 0));
    private static final List<BlockPos> Z_INTERIOR =
            listCellsBetween(ORIGIN, ORIGIN.offset(0, PortalFrame.MIN_HEIGHT - 1, PortalFrame.MIN_WIDTH - 1));
    private static final List<BlockPos> FRONT_EXIT =
            listCellsBetween(ORIGIN.south(), ORIGIN.south().offset(PortalFrame.MIN_WIDTH - 1, EXIT_HEADROOM - 1, 0));
    private static final List<BlockPos> BACK_EXIT =
            listCellsBetween(ORIGIN.north(), ORIGIN.north().offset(PortalFrame.MIN_WIDTH - 1, EXIT_HEADROOM - 1, 0));
    private static final List<BlockPos> X_FRAME_SAMPLES = List.of(
            ORIGIN.west(), ORIGIN.east(PortalFrame.MIN_WIDTH), ORIGIN.above(PortalFrame.MIN_HEIGHT), ORIGIN.below());
    private static final List<BlockPos> Z_FRAME_SAMPLES = List.of(
            ORIGIN.north(), ORIGIN.south(PortalFrame.MIN_WIDTH), ORIGIN.above(PortalFrame.MIN_HEIGHT), ORIGIN.below());

    private PortalGameTests() {}

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void build_places_portal_blocks_east_of_origin_when_axis_is_x(GameTestHelper helper) {
        buildOpenPortalAtOrigin(helper, Axis.X);

        assertInteriorHolds(helper, getPortalState(Axis.X));
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void build_surrounds_portal_with_frame_when_axis_is_x(GameTestHelper helper) {
        buildOpenPortalAtOrigin(helper, Axis.X);

        helper.assertValueEqual(
                readStates(helper, X_FRAME_SAMPLES), listUniformStates(X_FRAME_SAMPLES, getFrameState()), "frame");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void build_places_portal_blocks_south_of_origin_when_axis_is_z(GameTestHelper helper) {
        buildOpenPortalAtOrigin(helper, Axis.Z);

        helper.assertValueEqual(
                readStates(helper, Z_INTERIOR), listUniformStates(Z_INTERIOR, getPortalState(Axis.Z)), "interior");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void build_surrounds_portal_with_frame_when_axis_is_z(GameTestHelper helper) {
        buildOpenPortalAtOrigin(helper, Axis.Z);

        helper.assertValueEqual(
                readStates(helper, Z_FRAME_SAMPLES), listUniformStates(Z_FRAME_SAMPLES, getFrameState()), "frame");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void build_clears_exit_space_when_site_is_forced(GameTestHelper helper) {
        blockCells(helper, FRONT_EXIT);

        buildPortal(helper, new PortalSite.Forced(locateFootprintAtOrigin(helper, Axis.X)));

        helper.assertValueEqual(
                readStates(helper, FRONT_EXIT), listUniformStates(FRONT_EXIT, Blocks.AIR.defaultBlockState()), "exit");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void find_portal_around_returns_portal_when_it_sits_far_above_target(GameTestHelper helper) {
        DimensionPortalBlock portal =
                PortalRegisters.getPortalBlock(PortalVariants.OVERWORLD_MINING).get();
        BlockPos portalPos = new BlockPos(3, 5, 3);
        helper.setBlock(portalPos, portal.defaultBlockState());

        Optional<BlockPos> found = new ExitPortalFinder(helper.getLevel(), PortalVariants.OVERWORLD_MINING)
                .findPortalAround(helper.absolutePos(FLOOR_CENTER));

        helper.assertValueEqual(found, Optional.of(helper.absolutePos(portalPos)), "found portal");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void find_portal_around_returns_farther_portal_when_nearer_poi_holds_vanilla_nether_portal(
            GameTestHelper helper) {
        BlockState portalState = PortalRegisters.getPortalBlock(PortalVariants.OVERWORLD_MINING)
                .get()
                .defaultBlockState();
        BlockPos stalePos = helper.absolutePos(ORIGIN);
        helper.setBlock(ORIGIN, portalState);
        helper.getLevel().getChunkAt(stalePos).setBlockState(stalePos, Blocks.NETHER_PORTAL.defaultBlockState(), false);
        BlockPos portalPos = new BlockPos(3, 5, 3);
        helper.setBlock(portalPos, portalState);

        Optional<BlockPos> found = new ExitPortalFinder(helper.getLevel(), PortalVariants.OVERWORLD_MINING)
                .findPortalAround(helper.absolutePos(FLOOR_CENTER));

        helper.assertValueEqual(found, Optional.of(helper.absolutePos(portalPos)), "found portal");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void select_returns_back_side_when_front_is_blocked(GameTestHelper helper) {
        buildOpenPortalAtOrigin(helper, Axis.X);
        blockCells(helper, FRONT_EXIT);

        PortalExit exit = selectExitAtOrigin(helper);

        Vec3 behindPortalSeam =
                Vec3.atBottomCenterOf(helper.absolutePos(ORIGIN.north())).add(0.5, 0.0, 0.0);
        helper.assertValueEqual(exit, new PortalExit(behindPortalSeam, Direction.NORTH.toYRot()), "exit");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void select_returns_inside_portal_when_both_sides_are_blocked(GameTestHelper helper) {
        buildOpenPortalAtOrigin(helper, Axis.X);
        blockCells(helper, FRONT_EXIT);
        blockCells(helper, BACK_EXIT);

        PortalExit exit = selectExitAtOrigin(helper);

        Vec3 portalSeam = Vec3.atBottomCenterOf(helper.absolutePos(ORIGIN)).add(0.5, 0.0, 0.0);
        helper.assertValueEqual(exit, new PortalExit(portalSeam, Direction.SOUTH.toYRot()), "exit");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void can_place_liquid_keeps_portal_intact_when_water_flows_against_it(GameTestHelper helper) {
        buildOpenPortalAtOrigin(helper, Axis.X);

        helper.setBlock(ORIGIN.south(), Blocks.WATER);

        helper.runAfterDelay(WATER_FLOW_TICKS, () -> {
            assertInteriorHolds(helper, getPortalState(Axis.X));
            helper.succeed();
        });
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7, skyAccess = true)
    public static void search_spiral_on_surface_returns_south_east_neighbor_when_container_sits_on_target_column(
            GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos center = helper.absolutePos(FLOOR_CENTER);
        BlockPos chest = center.atY(level.getHeight(Heightmap.Types.MOTION_BLOCKING, center.getX(), center.getZ()));
        level.setBlockAndUpdate(chest, Blocks.CHEST.defaultBlockState());

        Optional<PortalFootprint> result =
                PortalPositionFinder.searchSpiralOnSurface(level, new PortalFootprint(center, Axis.X));

        PortalFootprint expected = new PortalFootprint(chest.east().south(), Axis.X);
        helper.assertValueEqual(result, Optional.of(expected), "site");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_fills_frame_when_corners_are_default_corner_blocks(GameTestHelper helper) {
        PortalVariant variant = pickVariant();
        createFrameBuilder(helper, variant)
                .withCornerBlock(variant.defaultCornerBlock())
                .build(locateFrameAtOrigin(helper, Axis.X));

        clickFrame(helper, makePlayerHoldingFlintAndSteel(helper, GameType.SURVIVAL));

        assertInteriorHolds(helper, getPortalState(variant, Axis.X));
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_leaves_frame_empty_when_corners_are_frame_blocks(GameTestHelper helper) {
        createFrameBuilder(helper, pickVariant()).build(locateFrameAtOrigin(helper, Axis.X));

        clickFrame(helper, makePlayerHoldingFlintAndSteel(helper, GameType.SURVIVAL));

        assertInteriorHolds(helper, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_leaves_vanilla_item_use_alone_when_corners_are_frame_blocks(
            GameTestHelper helper) {
        createFrameBuilder(helper, pickVariant()).build(locateFrameAtOrigin(helper, Axis.X));

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
                .build(locateFrameAtOrigin(helper, Axis.X));

        clickFrame(helper, makePlayerHoldingFlintAndSteel(helper, GameType.SURVIVAL));

        assertInteriorHolds(helper, getPortalState(PortalVariants.NETHER_MINING, Axis.X));
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_fills_frame_when_corners_are_infested_corner_blocks(GameTestHelper helper) {
        createFrameBuilder(helper, PortalVariants.NETHER_MINING)
                .withCornerBlock(Blocks.INFESTED_STONE)
                .build(locateFrameAtOrigin(helper, Axis.X));

        clickFrame(helper, makePlayerHoldingFlintAndSteel(helper, GameType.SURVIVAL));

        assertInteriorHolds(helper, getPortalState(PortalVariants.NETHER_MINING, Axis.X));
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void create_rejects_corners_when_they_are_the_frame_block_of_any_variant(GameTestHelper helper) {
        List<Boolean> accepted = PortalVariants.ALL.stream()
                .map(variant -> PortalCornerMaterials.create(variant)
                        .acceptsAll(PortalCornersFaker.fillCorners(
                                variant.frameBlock().defaultBlockState())))
                .toList();

        helper.assertValueEqual(accepted, Collections.nCopies(PortalVariants.ALL.size(), false), "accepted");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void create_accepts_corners_when_they_are_the_default_corner_block_of_any_variant(
            GameTestHelper helper) {
        List<Boolean> accepted = PortalVariants.ALL.stream()
                .map(variant -> PortalCornerMaterials.create(variant)
                        .acceptsAll(PortalCornersFaker.fillCorners(
                                variant.defaultCornerBlock().defaultBlockState())))
                .toList();

        helper.assertValueEqual(accepted, Collections.nCopies(PortalVariants.ALL.size(), true), "accepted");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_leaves_frame_empty_when_player_is_in_adventure_mode(GameTestHelper helper) {
        buildFrameAroundOrigin(helper);

        clickFrame(helper, makePlayerHoldingFlintAndSteel(helper, GameType.ADVENTURE));

        assertInteriorHolds(helper, Blocks.AIR.defaultBlockState());
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

        assertInteriorHolds(helper, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_leaves_frame_empty_when_item_use_is_denied(GameTestHelper helper) {
        buildFrameAroundOrigin(helper);
        PlayerInteractEvent.RightClickBlock event =
                createClick(helper, makePlayerHoldingFlintAndSteel(helper, GameType.SURVIVAL));
        event.setUseItem(TriState.FALSE);

        NeoForge.EVENT_BUS.post(event);

        assertInteriorHolds(helper, Blocks.AIR.defaultBlockState());
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_fills_frame_when_fire_burns_inside(GameTestHelper helper) {
        buildFrameAroundOrigin(helper);
        helper.setBlock(ORIGIN.east(), Blocks.FIRE);

        clickFrame(helper, makePlayerHoldingFlintAndSteel(helper, GameType.SURVIVAL));

        assertInteriorHolds(helper, getPortalState(Axis.X));
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_keeps_existing_portal_axis_when_frame_already_holds_portal_blocks(
            GameTestHelper helper) {
        buildFrameAroundOrigin(helper);
        BlockPos topInterior = ORIGIN.above(PortalFrame.MIN_HEIGHT - 1);
        helper.setBlock(topInterior, getPortalState(Axis.Z));

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
    public static void get_portal_transition_time_matches_vanilla_delay_when_player_is_in_survival(
            GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        int actual = PortalRegisters.getPortalBlock(PortalVariants.OVERWORLD_MINING)
                .get()
                .getPortalTransitionTime(helper.getLevel(), player);

        helper.assertValueEqual(actual, getVanillaPortalDelay(helper), "transition time");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void choose_route_travels_to_egmd_dimension_when_cow_steps_in_from_overworld(GameTestHelper helper) {
        Cow cow = helper.spawn(EntityType.COW, SPAWN);
        ResourceKey<Level> destination = EgmdDimensionFaker.pickRegistered().levelKey();

        DimensionPortalBlock.PortalRoute route = DimensionPortalBlock.chooseRoute(cow, destination);

        helper.assertValueEqual(route, new DimensionPortalBlock.PortalRoute.Travel(destination), "route");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void choose_route_leaves_portal_unlinked_when_portal_stands_in_another_dimension(
            GameTestHelper helper) {
        ServerLevel elsewhere = Objects.requireNonNull(
                helper.getLevel().getServer().getLevel(FAKER.options().option(Level.NETHER, Level.END)));
        Cow cow = Objects.requireNonNull(EntityType.COW.create(elsewhere));

        DimensionPortalBlock.PortalRoute route = DimensionPortalBlock.chooseRoute(
                cow, EgmdDimensionFaker.pickRegistered().levelKey());

        helper.assertValueEqual(route, new DimensionPortalBlock.PortalRoute.Unlinked(), "route");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void choose_route_refuses_zombie_when_destination_forbids_hostile_mobs(GameTestHelper helper) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, SPAWN);
        ResourceKey<Level> destination = EgmdDimensionFaker.pickRegistered(
                        dimension -> dimension.restrictions().contains(Restriction.NO_HOSTILE_MOBS))
                .levelKey();

        DimensionPortalBlock.PortalRoute route = DimensionPortalBlock.chooseRoute(zombie, destination);

        helper.assertValueEqual(route, new DimensionPortalBlock.PortalRoute.Refused(), "route");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void choose_route_travels_with_zombie_when_destination_allows_hostile_mobs(GameTestHelper helper) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, SPAWN);
        ResourceKey<Level> destination = EgmdDimensionFaker.pickRegistered(
                        dimension -> !dimension.restrictions().contains(Restriction.NO_HOSTILE_MOBS))
                .levelKey();

        DimensionPortalBlock.PortalRoute route = DimensionPortalBlock.chooseRoute(zombie, destination);

        helper.assertValueEqual(route, new DimensionPortalBlock.PortalRoute.Travel(destination), "route");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void update_shape_returns_air_when_portal_state_is_placed_on_empty_cell(GameTestHelper helper) {
        BlockState placed = Block.updateFromNeighbourShapes(
                getPortalState(PortalFootprintFaker.generateAxis()), helper.getLevel(), helper.absolutePos(ORIGIN));

        helper.assertValueEqual(placed, Blocks.AIR.defaultBlockState(), "placed state");
        helper.succeed();
    }

    private static List<BlockPos> listCellsBetween(BlockPos first, BlockPos last) {
        return BlockPos.betweenClosedStream(first, last)
                .map(BlockPos::immutable)
                .toList();
    }

    private static BlockState getPortalState(Axis axis) {
        return getPortalState(PortalVariants.OVERWORLD_MINING, axis);
    }

    private static BlockState getPortalState(PortalVariant variant, Axis axis) {
        return PortalRegisters.getPortalBlock(variant)
                .get()
                .defaultBlockState()
                .setValue(DimensionPortalBlock.AXIS, axis);
    }

    private static PortalVariant pickVariant() {
        return FAKER.options().nextElement(PortalVariants.ALL);
    }

    private static int getVanillaPortalDelay(GameTestHelper helper) {
        return helper.getLevel().getGameRules().getInt(GameRules.RULE_PLAYERS_NETHER_PORTAL_DEFAULT_DELAY);
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

    private static PortalFrame locateFrameAtOrigin(GameTestHelper helper, Axis axis) {
        return new PortalFrame(
                new BlockUtil.FoundRectangle(helper.absolutePos(ORIGIN), PortalFrame.MIN_WIDTH, PortalFrame.MIN_HEIGHT),
                axis);
    }

    private static PortalFootprint locateFootprintAtOrigin(GameTestHelper helper, Axis axis) {
        return new PortalFootprint(helper.absolutePos(ORIGIN), axis);
    }

    private static PortalFrameBuilder createFrameBuilder(GameTestHelper helper, PortalVariant variant) {
        return new PortalFrameBuilder(helper.getLevel(), variant.frameBlock());
    }

    private static void buildFrameAroundOrigin(GameTestHelper helper) {
        createFrameBuilder(helper, PortalVariants.OVERWORLD_MINING)
                .withCornerBlock(PortalVariants.OVERWORLD_MINING.defaultCornerBlock())
                .build(locateFrameAtOrigin(helper, Axis.X));
    }

    private static void buildOpenPortalAtOrigin(GameTestHelper helper, Axis axis) {
        buildPortal(helper, new PortalSite.Open(locateFootprintAtOrigin(helper, axis)));
    }

    private static void buildPortal(GameTestHelper helper, PortalSite site) {
        new PortalStructureBuilder(
                        PortalRegisters.getPortalBlock(PortalVariants.OVERWORLD_MINING)
                                .get(),
                        new PortalCornersFaker().generate())
                .build(helper.getLevel(), site);
    }

    private static PortalExit selectExitAtOrigin(GameTestHelper helper) {
        return new PortalExitSelector(helper.getLevel(), helper.makeMockPlayer(GameType.SURVIVAL))
                .select(locateFrameAtOrigin(helper, Axis.X));
    }

    private static BlockState getFrameState() {
        return PortalRegisters.getPortalBlock(PortalVariants.OVERWORLD_MINING)
                .get()
                .getFrameBlock()
                .defaultBlockState();
    }

    private static void blockCells(GameTestHelper helper, List<BlockPos> cells) {
        cells.forEach(cell -> helper.setBlock(cell, Blocks.STONE));
    }

    private static List<BlockState> readStates(GameTestHelper helper, List<BlockPos> cells) {
        return cells.stream().map(helper::getBlockState).toList();
    }

    private static List<BlockState> listUniformStates(List<BlockPos> cells, BlockState state) {
        return Collections.nCopies(cells.size(), state);
    }

    private static void assertInteriorHolds(GameTestHelper helper, BlockState expected) {
        helper.assertValueEqual(readStates(helper, X_INTERIOR), listUniformStates(X_INTERIOR, expected), "interior");
    }
}
