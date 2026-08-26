package com.ejafi.egmd.content.block;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.core.registration.ModBlocks;
import com.ejafi.egmd.core.registration.PortalVariants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

/**
 * In-world integration tests for the portal machinery that unit tests cannot cover.
 */
@GameTestHolder(EGMD.MODID)
public class PortalGameTests {
    private static final String TEMPLATE = "empty_7x7x7";

    private PortalGameTests() {}

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void structure_builder_places_x_axis_portal_east_of_origin(GameTestHelper helper) {
        BlockPos origin = new BlockPos(2, 2, 2);
        MiningPortalBlock portal = overworldPortal();

        PortalStructureBuilder.build(helper.getLevel(), helper.absolutePos(origin), Axis.X, portal);

        helper.assertBlockPresent(portal, origin);
        helper.assertBlockPresent(portal, origin.east());
        helper.assertBlockProperty(origin, MiningPortalBlock.AXIS, Axis.X);
        helper.assertBlockPresent(portal.getFrameBlock(), origin.west());
        helper.assertBlockPresent(portal.getFrameBlock(), origin.east(2));
        helper.assertBlockPresent(portal.getFrameBlock(), origin.above(MiningPortalShape.MIN_HEIGHT));
        helper.assertBlockPresent(portal.getFrameBlock(), origin.below());
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void structure_builder_places_z_axis_portal_south_of_origin(GameTestHelper helper) {
        BlockPos origin = new BlockPos(2, 2, 2);
        MiningPortalBlock portal = overworldPortal();

        PortalStructureBuilder.build(helper.getLevel(), helper.absolutePos(origin), Axis.Z, portal);

        helper.assertBlockPresent(portal, origin);
        helper.assertBlockPresent(portal, origin.south());
        helper.assertBlockProperty(origin, MiningPortalBlock.AXIS, Axis.Z);
        helper.assertBlockPresent(portal.getFrameBlock(), origin.north());
        helper.assertBlockPresent(portal.getFrameBlock(), origin.south(2));
        helper.assertBlockPresent(portal.getFrameBlock(), origin.below());
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void fallback_search_descends_to_surface_below_zero(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos column = helper.absolutePos(new BlockPos(3, 3, 3));
        clearColumn(level, column.getX(), column.getZ());
        level.setBlockAndUpdate(new BlockPos(column.getX(), -40, column.getZ()), Blocks.STONE.defaultBlockState());

        Optional<BlockPos> result = new FallbackSearch()
                .search(level, column, level.getMaxBuildHeight() - 1);

        BlockPos expected = new BlockPos(column.getX(), -39, column.getZ());
        if (!Optional.of(expected).equals(result)) {
            helper.fail("Expected " + expected + " but was " + result);
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void spiral_search_returns_nearest_open_column_at_surface(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos center = helper.absolutePos(new BlockPos(3, 3, 3));
        level.setBlockAndUpdate(new BlockPos(center.getX(), 40, center.getZ()), Blocks.STONE.defaultBlockState());

        Optional<BlockPos> result = new SpiralSearch()
                .search(level, center, level.getMaxBuildHeight() - 1);

        BlockPos expected = new BlockPos(center.getX(), 41, center.getZ());
        if (!Optional.of(expected).equals(result)) {
            helper.fail("Expected " + expected + " but was " + result);
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void ignition_fills_frame_with_portal_blocks(GameTestHelper helper) {
        MiningPortalBlock portal = overworldPortal();
        buildFrame(helper, portal);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FLINT_AND_STEEL));
        BlockPos clickedFrame = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(clickedFrame), Direction.UP, clickedFrame, false);

        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, clickedFrame, hit));

        helper.assertBlockPresent(portal, new BlockPos(2, 2, 2));
        helper.assertBlockPresent(portal, new BlockPos(3, 2, 2));
        helper.assertBlockPresent(portal, new BlockPos(2, 4, 2));
        helper.assertBlockPresent(portal, new BlockPos(3, 4, 2));
        helper.succeed();
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

    private static void clearColumn(ServerLevel level, int x, int z) {
        for (int y = level.getMaxBuildHeight() - 1; y >= level.getMinBuildHeight(); y--) {
            level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
        }
    }

    private static MiningPortalBlock overworldPortal() {
        return ModBlocks.PORTAL_BLOCKS.get(PortalVariants.OVERWORLD).get();
    }
}
