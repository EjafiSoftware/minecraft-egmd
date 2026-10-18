package com.ejafi.egmd.portal;

import static com.ejafi.egmd.portal.PortalGameTestSupport.ORIGIN;

import com.ejafi.egmd.Egmd;
import com.ejafi.egmd.GameTestTemplates;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Egmd.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PortalExitSelectorGameTests {
    private static final int EXIT_HEADROOM = 2;
    private static final List<BlockPos> FRONT_EXIT =
            listCellsBetween(ORIGIN.south(), ORIGIN.south().offset(PortalFrame.MIN_WIDTH - 1, EXIT_HEADROOM - 1, 0));
    private static final List<BlockPos> BACK_EXIT =
            listCellsBetween(ORIGIN.north(), ORIGIN.north().offset(PortalFrame.MIN_WIDTH - 1, EXIT_HEADROOM - 1, 0));

    private PortalExitSelectorGameTests() {}

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void select_returns_back_side_when_front_is_blocked(GameTestHelper helper) {
        PortalGameTestSupport.buildOpenPortalAtOrigin(helper, Axis.X);
        blockCells(helper, FRONT_EXIT);

        PortalExit exit = selectExitAtOrigin(helper);

        Vec3 behindPortalSeam =
                Vec3.atBottomCenterOf(helper.absolutePos(ORIGIN.north())).add(0.5, 0.0, 0.0);
        helper.assertValueEqual(exit, new PortalExit(behindPortalSeam, Direction.NORTH.toYRot()), "exit");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void select_returns_inside_portal_when_both_sides_are_blocked(GameTestHelper helper) {
        PortalGameTestSupport.buildOpenPortalAtOrigin(helper, Axis.X);
        blockCells(helper, FRONT_EXIT);
        blockCells(helper, BACK_EXIT);

        PortalExit exit = selectExitAtOrigin(helper);

        Vec3 portalSeam = Vec3.atBottomCenterOf(helper.absolutePos(ORIGIN)).add(0.5, 0.0, 0.0);
        helper.assertValueEqual(exit, new PortalExit(portalSeam, Direction.SOUTH.toYRot()), "exit");
        helper.succeed();
    }

    private static List<BlockPos> listCellsBetween(BlockPos first, BlockPos last) {
        return BlockPos.betweenClosedStream(first, last)
                .map(BlockPos::immutable)
                .toList();
    }

    private static void blockCells(GameTestHelper helper, List<BlockPos> cells) {
        cells.forEach(cell -> helper.setBlock(cell, Blocks.STONE));
    }

    private static PortalExit selectExitAtOrigin(GameTestHelper helper) {
        return new PortalExitSelector(helper.getLevel(), helper.makeMockPlayer(GameType.SURVIVAL))
                .select(PortalGameTestSupport.locateFrameAtOrigin(helper, Axis.X));
    }
}
