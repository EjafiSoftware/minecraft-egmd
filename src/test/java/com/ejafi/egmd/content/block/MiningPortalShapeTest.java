package com.ejafi.egmd.content.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ejafi.egmd.core.registration.ModBlocks;
import com.ejafi.egmd.core.registration.PortalVariants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

class MiningPortalShapeTest {
    private static final BlockPos INTERIOR_BOTTOM_LEFT = new BlockPos(0, 100, 0);

    private final FakeLevel level = new FakeLevel();

    @Test
    void isValid_reportsValidShape_whenInteriorMeetsMinimumSize() {
        buildFrame(MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT, Axis.X);

        MiningPortalShape shape = new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, portalBlock());

        assertTrue(shape.isValid());
        assertEquals(MiningPortalShape.MIN_HEIGHT, shape.getHeight());
    }

    @Test
    void isValid_reportsValidShape_whenFrameIsBuiltAlongZAxis() {
        buildFrame(MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT, Axis.Z);

        MiningPortalShape shape = new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.Z, portalBlock());

        assertTrue(shape.isValid());
        assertEquals(MiningPortalShape.MIN_HEIGHT, shape.getHeight());
    }

    @Test
    void isValid_rejectsShape_whenInteriorIsShorterThanMinimumHeight() {
        buildFrame(MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT - 1, Axis.X);

        MiningPortalShape shape = new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, portalBlock());

        assertFalse(shape.isValid());
    }

    @Test
    void isValid_rejectsShape_whenInteriorIsNarrowerThanMinimumWidth() {
        buildFrame(MiningPortalShape.MIN_WIDTH - 1, MiningPortalShape.MIN_HEIGHT, Axis.X);

        MiningPortalShape shape = new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, portalBlock());

        assertFalse(shape.isValid());
    }

    @Test
    void isValid_rejectsShape_whenTopFrameBlockIsMissing() {
        buildFrame(MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT, Axis.X);
        level.removeBlock(INTERIOR_BOTTOM_LEFT.offset(0, MiningPortalShape.MIN_HEIGHT, 0), false);

        MiningPortalShape shape = new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, portalBlock());

        assertFalse(shape.isValid());
    }

    @Test
    void getHeight_measuresWholeFrame_whenStartedFromTopInteriorBlock() {
        int interiorHeight = 4;
        buildFrame(MiningPortalShape.MIN_WIDTH, interiorHeight, Axis.X);
        BlockPos topInteriorBlock = INTERIOR_BOTTOM_LEFT.above(interiorHeight - 1);

        MiningPortalShape shape = new MiningPortalShape(level, topInteriorBlock, Axis.X, portalBlock());

        assertTrue(shape.isValid());
        assertEquals(interiorHeight, shape.getHeight());
    }

    @Test
    void createPortalBlocks_fillsInteriorWithAxisAlignedPortalBlocks_whenFrameIsValid() {
        buildFrame(MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT, Axis.X);
        MiningPortalShape shape = new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, portalBlock());

        shape.createPortalBlocks(level);

        for (int x = 0; x < MiningPortalShape.MIN_WIDTH; x++) {
            for (int y = 0; y < MiningPortalShape.MIN_HEIGHT; y++) {
                BlockState state = level.getBlockState(INTERIOR_BOTTOM_LEFT.offset(x, y, 0));
                assertEquals(portalBlock(), state.getBlock());
                assertEquals(Axis.X, state.getValue(MiningPortalBlock.AXIS));
            }
        }
    }

    @Test
    void isComplete_reportsComplete_whenInteriorIsFullyFilledWithPortalBlocks() {
        buildFrame(MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT, Axis.X);
        new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, portalBlock()).createPortalBlocks(level);

        MiningPortalShape shape = new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, portalBlock());

        assertTrue(shape.isComplete());
    }

    @Test
    void isComplete_reportsIncomplete_whenOneInteriorPortalBlockIsMissing() {
        buildFrame(MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT, Axis.X);
        new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, portalBlock()).createPortalBlocks(level);
        level.removeBlock(INTERIOR_BOTTOM_LEFT.above(1), false);

        MiningPortalShape shape = new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, portalBlock());

        assertTrue(shape.isValid());
        assertFalse(shape.isComplete());
    }

    private static MiningPortalBlock portalBlock() {
        return ModBlocks.PORTAL_BLOCKS.get(PortalVariants.OVERWORLD).get();
    }

    private void buildFrame(int interiorWidth, int interiorHeight, Axis axis) {
        for (int along = -1; along <= interiorWidth; along++) {
            setFrame(interiorPos(along, -1, axis));
            setFrame(interiorPos(along, interiorHeight, axis));
        }

        for (int y = 0; y < interiorHeight; y++) {
            setFrame(interiorPos(-1, y, axis));
            setFrame(interiorPos(interiorWidth, y, axis));
        }
    }

    private static BlockPos interiorPos(int along, int up, Axis axis) {
        return axis == Axis.X
                ? INTERIOR_BOTTOM_LEFT.offset(along, up, 0)
                : INTERIOR_BOTTOM_LEFT.offset(0, up, along);
    }

    private void setFrame(BlockPos pos) {
        level.setBlock(pos, portalBlock().getFrameBlock().defaultBlockState(), 3);
    }
}
