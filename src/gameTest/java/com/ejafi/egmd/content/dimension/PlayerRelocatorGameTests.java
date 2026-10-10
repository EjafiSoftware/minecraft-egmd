package com.ejafi.egmd.content.dimension;

import com.ejafi.egmd.EGMD;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * In-world tests for finding a place a relocated player can stand.
 */
@GameTestHolder(EGMD.MODID)
public class PlayerRelocatorGameTests {
    private static final String TEMPLATE = "empty_7x7x7";
    private static final BlockPos START = new BlockPos(3, 1, 3);

    private PlayerRelocatorGameTests() {}

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void find_clear_space_returns_first_open_spot_above_when_start_is_buried(GameTestHelper helper) {
        for (int y = 0; y < 3; y++) {
            helper.setBlock(START.above(y), Blocks.STONE);
        }
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        Optional<BlockPos> found = PlayerRelocator.findClearSpace(helper.getLevel(), player, helper.absolutePos(START));

        Optional<BlockPos> expected = Optional.of(helper.absolutePos(START.above(3)));
        if (!expected.equals(found)) {
            helper.fail("Expected " + expected + " but was " + found);
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void find_clear_space_returns_first_open_spot_above_when_only_head_is_buried(GameTestHelper helper) {
        helper.setBlock(START.above(), Blocks.STONE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        Optional<BlockPos> found = PlayerRelocator.findClearSpace(helper.getLevel(), player, helper.absolutePos(START));

        Optional<BlockPos> expected = Optional.of(helper.absolutePos(START.above(2)));
        if (!expected.equals(found)) {
            helper.fail("Expected " + expected + " but was " + found);
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void find_clear_space_returns_start_when_start_is_open(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        Optional<BlockPos> found = PlayerRelocator.findClearSpace(helper.getLevel(), player, helper.absolutePos(START));

        Optional<BlockPos> expected = Optional.of(helper.absolutePos(START));
        if (!expected.equals(found)) {
            helper.fail("Expected " + expected + " but was " + found);
        }
        helper.succeed();
    }
}
