package com.ejafi.egmd.portal;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.state.BlockState;

final class PortalGameTestSupport {
    static final BlockPos ORIGIN = new BlockPos(2, 2, 2);
    static final BlockPos FLOOR_CENTER = new BlockPos(3, 0, 3);

    private PortalGameTestSupport() {}

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
        assertStates(helper, mapInterior(locateFrameAtOrigin(helper, Axis.X), expected));
    }

    static void assertPortalFillsInterior(GameTestHelper helper, Axis axis) {
        assertStates(helper, mapInterior(locateFrameAtOrigin(helper, axis), getPortalState(axis)));
    }

    private static Map<BlockPos, BlockState> mapInterior(PortalFrame frame, BlockState state) {
        return frame.streamInterior().collect(Collectors.toUnmodifiableMap(BlockPos::immutable, pos -> state));
    }

    private static void assertStates(GameTestHelper helper, Map<BlockPos, BlockState> expected) {
        Map<BlockPos, BlockState> actual = expected.keySet().stream()
                .collect(Collectors.toUnmodifiableMap(Function.identity(), helper.getLevel()::getBlockState));
        helper.assertValueEqual(actual, expected, "interior");
    }
}
