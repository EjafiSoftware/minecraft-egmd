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
public final class ClearSpaceFinderGameTests {
    private static final Faker FAKER = new Faker();
    private static final int MAX_BURIAL_DEPTH = 4;

    private ClearSpaceFinderGameTests() {}

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void find_above_returns_first_open_spot_above_when_start_is_buried(GameTestHelper helper) {
        int depth = FAKER.number().numberBetween(1, MAX_BURIAL_DEPTH + 1);
        for (int y = 0; y < depth; y++) {
            helper.setBlock(GameTestTemplates.EMPTY_7X7X7_CENTER.above(y), Blocks.STONE);
        }

        Optional<BlockPos> found = findAboveStart(helper);

        helper.assertValueEqual(
                found,
                Optional.of(helper.absolutePos(GameTestTemplates.EMPTY_7X7X7_CENTER.above(depth))),
                "clear space");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void find_above_returns_first_open_spot_above_when_only_head_is_buried(GameTestHelper helper) {
        helper.setBlock(GameTestTemplates.EMPTY_7X7X7_CENTER.above(), Blocks.STONE);

        Optional<BlockPos> found = findAboveStart(helper);

        helper.assertValueEqual(
                found, Optional.of(helper.absolutePos(GameTestTemplates.EMPTY_7X7X7_CENTER.above(2))), "clear space");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void find_above_returns_start_when_start_is_open(GameTestHelper helper) {
        Optional<BlockPos> found = findAboveStart(helper);

        helper.assertValueEqual(
                found, Optional.of(helper.absolutePos(GameTestTemplates.EMPTY_7X7X7_CENTER)), "clear space");
        helper.succeed();
    }

    private static Optional<BlockPos> findAboveStart(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        return new ClearSpaceFinder(helper.getLevel(), player)
                .findAbove(helper.absolutePos(GameTestTemplates.EMPTY_7X7X7_CENTER));
    }
}
