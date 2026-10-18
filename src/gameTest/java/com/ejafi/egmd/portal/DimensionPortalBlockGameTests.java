package com.ejafi.egmd.portal;

import static com.ejafi.egmd.portal.PortalGameTestSupport.ORIGIN;

import com.ejafi.egmd.Egmd;
import com.ejafi.egmd.GameTestTemplates;
import com.ejafi.egmd.dimension.EgmdDimensionFaker;
import com.ejafi.egmd.dimension.Restriction;
import java.util.Objects;
import net.datafaker.Faker;
import net.minecraft.core.Direction.Axis;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Egmd.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DimensionPortalBlockGameTests {
    private static final Faker FAKER = new Faker();
    private static final int WATER_FLOW_TICKS = 40;

    private DimensionPortalBlockGameTests() {}

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void can_place_liquid_keeps_portal_intact_when_water_flows_against_it(GameTestHelper helper) {
        PortalGameTestSupport.buildOpenPortalAtOrigin(helper, Axis.X);

        helper.setBlock(ORIGIN.south(), Blocks.WATER);

        helper.runAfterDelay(WATER_FLOW_TICKS, () -> {
            PortalGameTestSupport.assertPortalFillsInterior(helper, Axis.X);
            helper.succeed();
        });
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
        Cow cow = helper.spawn(EntityType.COW, GameTestTemplates.EMPTY_7X7X7_CENTER);
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
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, GameTestTemplates.EMPTY_7X7X7_CENTER);
        ResourceKey<Level> destination = EgmdDimensionFaker.pickRegistered(
                        dimension -> dimension.restrictions().contains(Restriction.NO_HOSTILE_MOBS))
                .levelKey();

        DimensionPortalBlock.PortalRoute route = DimensionPortalBlock.chooseRoute(zombie, destination);

        helper.assertValueEqual(route, new DimensionPortalBlock.PortalRoute.Refused(), "route");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void choose_route_travels_with_zombie_when_destination_allows_hostile_mobs(GameTestHelper helper) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, GameTestTemplates.EMPTY_7X7X7_CENTER);
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
                PortalGameTestSupport.getPortalState(PortalFootprintFaker.generateAxis()),
                helper.getLevel(),
                helper.absolutePos(ORIGIN));

        helper.assertValueEqual(placed, Blocks.AIR.defaultBlockState(), "placed state");
        helper.succeed();
    }

    private static int getVanillaPortalDelay(GameTestHelper helper) {
        return helper.getLevel().getGameRules().getInt(GameRules.RULE_PLAYERS_NETHER_PORTAL_DEFAULT_DELAY);
    }
}
