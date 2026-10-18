package com.ejafi.egmd.dimension;

import com.ejafi.egmd.Egmd;
import com.ejafi.egmd.GameTestTemplates;
import java.util.Optional;
import net.datafaker.Faker;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Egmd.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PlayerRelocatorGameTests {
    private static final Faker FAKER = new Faker();
    private static final BlockPos START = new BlockPos(3, 1, 3);
    private static final int MAX_BURIAL_DEPTH = 4;

    private PlayerRelocatorGameTests() {}

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void find_clear_space_returns_first_open_spot_above_when_start_is_buried(GameTestHelper helper) {
        int depth = FAKER.number().numberBetween(1, MAX_BURIAL_DEPTH + 1);
        for (int y = 0; y < depth; y++) {
            helper.setBlock(START.above(y), Blocks.STONE);
        }

        Optional<BlockPos> found = findClearSpaceFromStart(helper);

        helper.assertValueEqual(found, Optional.of(helper.absolutePos(START.above(depth))), "clear space");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void find_clear_space_returns_first_open_spot_above_when_only_head_is_buried(GameTestHelper helper) {
        helper.setBlock(START.above(), Blocks.STONE);

        Optional<BlockPos> found = findClearSpaceFromStart(helper);

        helper.assertValueEqual(found, Optional.of(helper.absolutePos(START.above(2))), "clear space");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void find_clear_space_returns_start_when_start_is_open(GameTestHelper helper) {
        Optional<BlockPos> found = findClearSpaceFromStart(helper);

        helper.assertValueEqual(found, Optional.of(helper.absolutePos(START)), "clear space");
        helper.succeed();
    }

    private static Optional<BlockPos> findClearSpaceFromStart(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        return PlayerRelocator.findClearSpace(helper.getLevel(), player, helper.absolutePos(START));
    }
}
