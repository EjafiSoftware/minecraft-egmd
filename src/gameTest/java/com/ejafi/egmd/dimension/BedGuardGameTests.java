package com.ejafi.egmd.dimension;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.GameTestTemplates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(EGMD.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BedGuardGameTests {
    private static final BlockPos BED = new BlockPos(3, 1, 3);

    private BedGuardGameTests() {}

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void on_right_click_block_leaves_bed_usable_when_dimension_is_overworld(GameTestHelper helper) {
        helper.setBlock(BED, Blocks.RED_BED);
        BlockPos bed = helper.absolutePos(BED);

        PlayerInteractEvent.RightClickBlock event = NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(
                helper.makeMockPlayer(GameType.SURVIVAL),
                InteractionHand.MAIN_HAND,
                bed,
                new BlockHitResult(Vec3.atCenterOf(bed), Direction.UP, bed, false)));

        helper.assertValueEqual(event.getUseBlock(), TriState.DEFAULT, "bed use");
        helper.succeed();
    }
}
