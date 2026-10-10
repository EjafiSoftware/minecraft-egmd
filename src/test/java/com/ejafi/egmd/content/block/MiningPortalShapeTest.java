package com.ejafi.egmd.content.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ejafi.egmd.core.registration.ModBlocks;
import com.ejafi.egmd.core.registration.PortalVariants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

class MiningPortalShapeTest {
    private static final BlockPos INTERIOR_BOTTOM_LEFT = new BlockPos(0, 100, 0);

    private final FakeLevel level = new FakeLevel();

    @Test
    void isValid_reportsValidShape_whenInteriorMeetsMinimumSize() {
        buildFrame(MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT, Axis.X);

        MiningPortalShape shape = new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, getPortalBlock());

        assertTrue(shape.isValid());
    }

    @Test
    void isValid_reportsValidShape_whenFrameIsBuiltAlongZAxis() {
        buildFrame(MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT, Axis.Z);

        MiningPortalShape shape = new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.Z, getPortalBlock());

        assertTrue(shape.isValid());
    }

    @Test
    void isValid_rejectsShape_whenInteriorIsShorterThanMinimumHeight() {
        buildFrame(MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT - 1, Axis.X);

        MiningPortalShape shape = new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, getPortalBlock());

        assertFalse(shape.isValid());
    }

    @Test
    void isValid_rejectsShape_whenInteriorIsNarrowerThanMinimumWidth() {
        buildFrame(MiningPortalShape.MIN_WIDTH - 1, MiningPortalShape.MIN_HEIGHT, Axis.X);

        MiningPortalShape shape = new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, getPortalBlock());

        assertFalse(shape.isValid());
    }

    @Test
    void isValid_rejectsShape_whenTopFrameBlockIsMissing() {
        buildFrame(MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT, Axis.X);
        level.removeBlock(INTERIOR_BOTTOM_LEFT.offset(0, MiningPortalShape.MIN_HEIGHT, 0), false);

        MiningPortalShape shape = new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, getPortalBlock());

        assertFalse(shape.isValid());
    }

    @Test
    void getHeight_measuresWholeFrame_whenStartedFromTopInteriorBlock() {
        int interiorHeight = 4;
        buildFrame(MiningPortalShape.MIN_WIDTH, interiorHeight, Axis.X);
        BlockPos topInteriorBlock = INTERIOR_BOTTOM_LEFT.above(interiorHeight - 1);

        MiningPortalShape shape = new MiningPortalShape(level, topInteriorBlock, Axis.X, getPortalBlock());

        assertEquals(interiorHeight, shape.getHeight());
    }

    @Test
    void createPortalBlocks_fillsInteriorWithAxisAlignedPortalBlocks_whenFrameIsValid() {
        buildFrame(MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT, Axis.X);
        MiningPortalShape shape = new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, getPortalBlock());

        shape.createPortalBlocks(level);

        BlockState expected = getPortalBlock().defaultBlockState().setValue(MiningPortalBlock.AXIS, Axis.X);
        for (int x = 0; x < MiningPortalShape.MIN_WIDTH; x++) {
            for (int y = 0; y < MiningPortalShape.MIN_HEIGHT; y++) {
                assertEquals(expected, level.getBlockState(INTERIOR_BOTTOM_LEFT.offset(x, y, 0)));
            }
        }
    }

    @Test
    void isComplete_reportsComplete_whenInteriorIsFullyFilledWithPortalBlocks() {
        buildFrame(MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT, Axis.X);
        new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, getPortalBlock()).createPortalBlocks(level);

        MiningPortalShape shape = new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, getPortalBlock());

        assertTrue(shape.isComplete());
    }

    @Test
    void isComplete_reportsIncomplete_whenOneInteriorPortalBlockIsMissing() {
        buildFrame(MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT, Axis.X);
        new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, getPortalBlock()).createPortalBlocks(level);
        level.removeBlock(INTERIOR_BOTTOM_LEFT.above(1), false);

        MiningPortalShape shape = new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, getPortalBlock());

        assertFalse(shape.isComplete());
    }

    @Test
    void isValid_reportsValidShape_whenFireBurnsInsideTheFrame() {
        buildFrame(MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT, Axis.X);
        level.setBlock(INTERIOR_BOTTOM_LEFT.east(), Blocks.FIRE.defaultBlockState(), 3);

        MiningPortalShape shape = new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, getPortalBlock());

        assertTrue(shape.isValid());
    }

    @Test
    void isEmptyFrame_rejectsShape_whenInteriorAlreadyHoldsPortalBlocks() {
        buildFrame(MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT, Axis.X);
        level.setBlock(INTERIOR_BOTTOM_LEFT.above(), getPortalBlock().defaultBlockState().setValue(MiningPortalBlock.AXIS, Axis.Z), 3);

        MiningPortalShape shape = new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, getPortalBlock());

        assertFalse(shape.isEmptyFrame());
    }

    @Test
    void isEmptyFrame_acceptsShape_whenInteriorIsAir() {
        buildFrame(MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT, Axis.X);

        MiningPortalShape shape = new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, getPortalBlock());

        assertTrue(shape.isEmptyFrame());
    }

    @Test
    void isComplete_reportsComplete_whenSilverfishInfestsAFrameBlock() {
        buildFrame(MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT, Axis.X);
        new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, getPortalBlock()).createPortalBlocks(level);
        level.setBlock(INTERIOR_BOTTOM_LEFT.west().above(), Blocks.INFESTED_COBBLESTONE.defaultBlockState(), 3);
        level.setBlock(INTERIOR_BOTTOM_LEFT.above(MiningPortalShape.MIN_HEIGHT), Blocks.INFESTED_COBBLESTONE.defaultBlockState(), 3);

        MiningPortalShape shape = new MiningPortalShape(level, INTERIOR_BOTTOM_LEFT, Axis.X, getPortalBlock());

        assertTrue(shape.isComplete());
    }

    @Test
    void isFrame_matchesBlock_whenItIsTheInfestedVariantOfTheFrameBlock() {
        assertTrue(MiningPortalShape.isFrame(Blocks.INFESTED_COBBLESTONE.defaultBlockState(), Blocks.COBBLESTONE));
    }

    @Test
    void isFrame_rejectsBlock_whenItIsTheInfestedVariantOfAnotherBlock() {
        assertFalse(MiningPortalShape.isFrame(Blocks.INFESTED_STONE.defaultBlockState(), Blocks.COBBLESTONE));
    }

    private static MiningPortalBlock getPortalBlock() {
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
        level.setBlock(pos, getPortalBlock().getFrameBlock().defaultBlockState(), 3);
    }
}
