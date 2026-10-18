package com.ejafi.egmd.dimension;

import com.ejafi.egmd.Egmd;
import com.ejafi.egmd.GameTestTemplates;
import net.datafaker.Faker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Egmd.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BedGuardGameTests {
    private static final Faker FAKER = new Faker();

    private BedGuardGameTests() {}

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_leaves_bed_usable_when_dimension_is_overworld(GameTestHelper helper) {
        helper.setBlock(GameTestTemplates.EMPTY_7X7X7_CENTER, Blocks.RED_BED);
        BlockPos bed = helper.absolutePos(GameTestTemplates.EMPTY_7X7X7_CENTER);

        PlayerInteractEvent.RightClickBlock event = NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(
                helper.makeMockPlayer(GameType.SURVIVAL),
                InteractionHand.MAIN_HAND,
                bed,
                new BlockHitResult(Vec3.atCenterOf(bed), Direction.UP, bed, false)));

        helper.assertValueEqual(event.getUseBlock(), TriState.DEFAULT, "bed use");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void bypasses_block_use_returns_true_when_sneaking_player_holds_item(GameTestHelper helper) {
        Player player = makePlayerHoldingItem(helper);
        player.setShiftKeyDown(true);

        boolean bypassed = BedGuard.bypassesBlockUse(player, helper.absolutePos(GameTestTemplates.EMPTY_7X7X7_CENTER));

        helper.assertTrue(bypassed, "sneaking player holding an item should bypass bed use");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void bypasses_block_use_returns_false_when_sneaking_player_has_empty_hands(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true);

        boolean bypassed = BedGuard.bypassesBlockUse(player, helper.absolutePos(GameTestTemplates.EMPTY_7X7X7_CENTER));

        helper.assertFalse(bypassed, "sneaking player with empty hands should not bypass bed use");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void bypasses_block_use_returns_false_when_player_is_not_sneaking(GameTestHelper helper) {
        Player player = makePlayerHoldingItem(helper);

        boolean bypassed = BedGuard.bypassesBlockUse(player, helper.absolutePos(GameTestTemplates.EMPTY_7X7X7_CENTER));

        helper.assertFalse(bypassed, "player who is not sneaking should not bypass bed use");
        helper.succeed();
    }

    private static Player makePlayerHoldingItem(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(
                FAKER.options().option(InteractionHand.class),
                new ItemStack(FAKER.options().option(Items.STONE, Items.TORCH, Items.OAK_PLANKS)));
        return player;
    }
}
