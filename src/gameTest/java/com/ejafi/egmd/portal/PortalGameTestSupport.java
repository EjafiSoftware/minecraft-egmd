package com.ejafi.egmd.portal;

import java.util.Collections;
import java.util.List;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.state.BlockState;

final class PortalGameTestSupport {
    static final BlockPos ORIGIN = new BlockPos(2, 2, 2);
    static final BlockPos FLOOR_CENTER = new BlockPos(3, 0, 3);
    private static final List<BlockPos> X_INTERIOR =
            listCellsBetween(ORIGIN, ORIGIN.offset(PortalFrame.MIN_WIDTH - 1, PortalFrame.MIN_HEIGHT - 1, 0));

    private PortalGameTestSupport() {}

    static List<BlockPos> listCellsBetween(BlockPos first, BlockPos last) {
        return BlockPos.betweenClosedStream(first, last)
                .map(BlockPos::immutable)
                .toList();
    }

    static BlockState getPortalState(Axis axis) {
        return getPortalState(PortalVariants.OVERWORLD_MINING, axis);
    }

    static BlockState getPortalState(PortalVariant variant, Axis axis) {
        return PortalRegisters.getPortalBlock(variant)
                .get()
                .defaultBlockState()
                .setValue(DimensionPortalBlock.AXIS, axis);
    }

    static PortalFrame locateFrameAtOrigin(GameTestHelper helper, Axis axis) {
        return new PortalFrame(
                new BlockUtil.FoundRectangle(helper.absolutePos(ORIGIN), PortalFrame.MIN_WIDTH, PortalFrame.MIN_HEIGHT),
                axis);
    }

    static void buildOpenPortalAtOrigin(GameTestHelper helper, Axis axis) {
        new PortalStructureBuilder(
                        PortalRegisters.getPortalBlock(PortalVariants.OVERWORLD_MINING)
                                .get(),
                        new PortalCornersFaker().generate())
                .build(helper.getLevel(), new PortalSite.Open(new PortalFootprint(helper.absolutePos(ORIGIN), axis)));
    }

    static void assertInteriorHolds(GameTestHelper helper, BlockState expected) {
        helper.assertValueEqual(readStates(helper, X_INTERIOR), listUniformStates(X_INTERIOR, expected), "interior");
    }

    static List<BlockState> readStates(GameTestHelper helper, List<BlockPos> cells) {
        return cells.stream().map(helper::getBlockState).toList();
    }

    static List<BlockState> listUniformStates(List<BlockPos> cells, BlockState state) {
        return Collections.nCopies(cells.size(), state);
    }
}
