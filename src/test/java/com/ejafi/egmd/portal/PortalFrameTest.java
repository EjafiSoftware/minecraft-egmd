package com.ejafi.egmd.portal;

import static org.assertj.core.api.Assertions.assertThat;

import com.ejafi.egmd.BlockPosFaker;
import java.util.List;
import java.util.Map;
import net.datafaker.Faker;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

class PortalFrameTest {
    private static final Faker FAKER = new Faker();

    private final FakeLevel level = new FakeLevel();
    private final DimensionPortalBlock portalBlock =
            PortalRegisters.getPortalBlock(PortalVariants.OVERWORLD_MINING).get();
    private final PortalFrameBuilder frameBuilder = new PortalFrameBuilder(level, portalBlock.getFrameBlock());
    private final BlockPos interiorBottomLeft = new BlockPosFaker()
            .withY(FAKER.number()
                    .numberBetween(
                            level.getMinBuildHeight() + 1, level.getMaxBuildHeight() - PortalFrame.MAX_HEIGHT - 2))
            .generate();
    private final int width = FAKER.number().numberBetween(PortalFrame.MIN_WIDTH, PortalFrame.MAX_WIDTH + 1);
    private final int height = FAKER.number().numberBetween(PortalFrame.MIN_HEIGHT, PortalFrame.MAX_HEIGHT + 1);
    private final BlockUtil.FoundRectangle interior = new BlockUtil.FoundRectangle(interiorBottomLeft, width, height);

    @Test
    void isValid_reportsValidShape_whenInteriorMeetsMinimumSize() {
        frameBuilder.build(
                new BlockUtil.FoundRectangle(interiorBottomLeft, PortalFrame.MIN_WIDTH, PortalFrame.MIN_HEIGHT),
                Axis.X);

        var shape = new PortalFrame(level, interiorBottomLeft, Axis.X, portalBlock);

        assertThat(shape.isValid()).isTrue();
    }

    @Test
    void isValid_reportsValidShape_whenFrameIsBuiltAlongZAxis() {
        frameBuilder.build(interior, Axis.Z);

        var shape = new PortalFrame(level, interiorBottomLeft, Axis.Z, portalBlock);

        assertThat(shape.isValid()).isTrue();
    }

    @Test
    void isValid_rejectsShape_whenInteriorIsShorterThanMinimumHeight() {
        frameBuilder.build(new BlockUtil.FoundRectangle(interiorBottomLeft, width, PortalFrame.MIN_HEIGHT - 1), Axis.X);

        var shape = new PortalFrame(level, interiorBottomLeft, Axis.X, portalBlock);

        assertThat(shape.isValid()).isFalse();
    }

    @Test
    void isValid_rejectsShape_whenInteriorIsNarrowerThanMinimumWidth() {
        frameBuilder.build(new BlockUtil.FoundRectangle(interiorBottomLeft, PortalFrame.MIN_WIDTH - 1, height), Axis.X);

        var shape = new PortalFrame(level, interiorBottomLeft, Axis.X, portalBlock);

        assertThat(shape.isValid()).isFalse();
    }

    @Test
    void isValid_reportsValidShape_whenInteriorIsMaximumWidth() {
        frameBuilder.build(new BlockUtil.FoundRectangle(interiorBottomLeft, PortalFrame.MAX_WIDTH, height), Axis.X);

        var shape = new PortalFrame(level, interiorBottomLeft, Axis.X, portalBlock);

        assertThat(shape.isValid()).isTrue();
    }

    @Test
    void isValid_rejectsShape_whenInteriorIsWiderThanMaximumWidth() {
        frameBuilder.build(new BlockUtil.FoundRectangle(interiorBottomLeft, PortalFrame.MAX_WIDTH + 1, height), Axis.X);

        var shape = new PortalFrame(level, interiorBottomLeft, Axis.X, portalBlock);

        assertThat(shape.isValid()).isFalse();
    }

    @Test
    void isValid_reportsValidShape_whenInteriorIsMaximumHeight() {
        frameBuilder.build(new BlockUtil.FoundRectangle(interiorBottomLeft, width, PortalFrame.MAX_HEIGHT), Axis.X);

        var shape = new PortalFrame(level, interiorBottomLeft, Axis.X, portalBlock);

        assertThat(shape.isValid()).isTrue();
    }

    @Test
    void isValid_rejectsShape_whenInteriorIsTallerThanMaximumHeight() {
        frameBuilder.build(new BlockUtil.FoundRectangle(interiorBottomLeft, width, PortalFrame.MAX_HEIGHT + 1), Axis.X);

        var shape = new PortalFrame(level, interiorBottomLeft, Axis.X, portalBlock);

        assertThat(shape.isValid()).isFalse();
    }

    @Test
    void isValid_rejectsShape_whenTopFrameBlockIsMissing() {
        frameBuilder.build(interior, Axis.X);
        level.removeBlock(interiorBottomLeft.above(height), false);

        var shape = new PortalFrame(level, interiorBottomLeft, Axis.X, portalBlock);

        assertThat(shape.isValid()).isFalse();
    }

    @Test
    void isValid_reportsValidShape_whenFireBurnsInsideTheFrame() {
        frameBuilder.build(interior, Axis.X);
        level.setBlock(
                FAKER.options().nextElement(listInteriorCells()), Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);

        var shape = new PortalFrame(level, interiorBottomLeft, Axis.X, portalBlock);

        assertThat(shape.isValid()).isTrue();
    }

    @Test
    void createPortalBlocks_fillsWholeInterior_whenStartedFromTopInteriorBlock() {
        frameBuilder.build(interior, Axis.X);
        var shape = new PortalFrame(level, interiorBottomLeft.above(height - 1), Axis.X, portalBlock);

        shape.createPortalBlocks(level);

        assertThat(readInteriorStates()).containsOnly(createPortalState(Axis.X));
    }

    @Test
    void createPortalBlocks_fillsInteriorWithAxisAlignedPortalBlocks_whenFrameIsValid() {
        frameBuilder.build(interior, Axis.X);
        var shape = new PortalFrame(level, interiorBottomLeft, Axis.X, portalBlock);

        shape.createPortalBlocks(level);

        assertThat(readInteriorStates()).containsOnly(createPortalState(Axis.X));
    }

    @Test
    void isComplete_reportsComplete_whenInteriorIsFullyFilledWithPortalBlocks() {
        frameBuilder.build(interior, Axis.X);
        new PortalFrame(level, interiorBottomLeft, Axis.X, portalBlock).createPortalBlocks(level);

        var shape = new PortalFrame(level, interiorBottomLeft, Axis.X, portalBlock);

        assertThat(shape.isComplete()).isTrue();
    }

    @Test
    void isComplete_reportsIncomplete_whenOneInteriorPortalBlockIsMissing() {
        frameBuilder.build(interior, Axis.X);
        new PortalFrame(level, interiorBottomLeft, Axis.X, portalBlock).createPortalBlocks(level);
        level.removeBlock(FAKER.options().nextElement(listInteriorCells()), false);

        var shape = new PortalFrame(level, interiorBottomLeft, Axis.X, portalBlock);

        assertThat(shape.isComplete()).isFalse();
    }

    @Test
    void isComplete_reportsComplete_whenSilverfishInfestsAFrameBlock() {
        frameBuilder.build(interior, Axis.X);
        new PortalFrame(level, interiorBottomLeft, Axis.X, portalBlock).createPortalBlocks(level);
        BlockState infested = Blocks.INFESTED_COBBLESTONE.defaultBlockState();
        level.setBlock(
                interiorBottomLeft.west().above(FAKER.number().numberBetween(0, height)), infested, Block.UPDATE_ALL);
        level.setBlock(interiorBottomLeft.above(height), infested, Block.UPDATE_ALL);

        var shape = new PortalFrame(level, interiorBottomLeft, Axis.X, portalBlock);

        assertThat(shape.isComplete()).isTrue();
    }

    @Test
    void isEmptyFrame_rejectsShape_whenInteriorAlreadyHoldsPortalBlocks() {
        frameBuilder.build(interior, Axis.X);
        level.setBlock(
                FAKER.options().nextElement(listInteriorCells()),
                createPortalState(PortalFootprintFaker.generateAxis()),
                Block.UPDATE_ALL);

        var shape = new PortalFrame(level, interiorBottomLeft, Axis.X, portalBlock);

        assertThat(shape.isEmptyFrame()).isFalse();
    }

    @Test
    void isEmptyFrame_acceptsShape_whenInteriorIsAir() {
        frameBuilder.build(interior, Axis.X);

        var shape = new PortalFrame(level, interiorBottomLeft, Axis.X, portalBlock);

        assertThat(shape.isEmptyFrame()).isTrue();
    }

    @Test
    void isFrame_matchesBlock_whenItIsTheInfestedVariantOfTheFrameBlock() {
        boolean frame = PortalFrame.isFrame(Blocks.INFESTED_COBBLESTONE.defaultBlockState(), Blocks.COBBLESTONE);

        assertThat(frame).isTrue();
    }

    @Test
    void isFrame_rejectsBlock_whenItIsTheInfestedVariantOfAnotherBlock() {
        boolean frame = PortalFrame.isFrame(Blocks.INFESTED_STONE.defaultBlockState(), Blocks.COBBLESTONE);

        assertThat(frame).isFalse();
    }

    @Test
    void resolveInfestedHost_returnsHostBlock_whenStateIsInfested() {
        Block host = PortalFrame.resolveInfestedHost(Blocks.INFESTED_COBBLESTONE.defaultBlockState());

        assertThat(host).isEqualTo(Blocks.COBBLESTONE);
    }

    @Test
    void resolveInfestedHost_returnsBlockItself_whenStateIsNotInfested() {
        Block block = FAKER.options().option(Blocks.COBBLESTONE, Blocks.NETHERRACK, Blocks.END_STONE);

        Block host = PortalFrame.resolveInfestedHost(block.defaultBlockState());

        assertThat(host).isEqualTo(block);
    }

    @Test
    void readCorners_returnsStatesDiagonalToInterior_whenFrameIsMinimumSizeAlongXAxis() {
        Map<PortalCorner, BlockState> corners = new PortalCornersFaker().generate();
        frameBuilder
                .withCorners(corners)
                .build(
                        new BlockUtil.FoundRectangle(interiorBottomLeft, PortalFrame.MIN_WIDTH, PortalFrame.MIN_HEIGHT),
                        Axis.X);

        var shape = new PortalFrame(level, interiorBottomLeft, Axis.X, portalBlock);

        assertThat(shape.readCorners()).isEqualTo(corners);
    }

    @Test
    void readCorners_returnsStatesDiagonalToInterior_whenFrameIsMinimumSizeAlongZAxis() {
        Map<PortalCorner, BlockState> corners = new PortalCornersFaker().generate();
        frameBuilder
                .withCorners(corners)
                .build(
                        new BlockUtil.FoundRectangle(interiorBottomLeft, PortalFrame.MIN_WIDTH, PortalFrame.MIN_HEIGHT),
                        Axis.Z);

        var shape = new PortalFrame(level, interiorBottomLeft, Axis.Z, portalBlock);

        assertThat(shape.readCorners()).isEqualTo(corners);
    }

    @Test
    void readCorners_returnsStatesDiagonalToInterior_whenFrameIsLargerThanMinimum() {
        Axis axis = PortalFootprintFaker.generateAxis();
        Map<PortalCorner, BlockState> corners = new PortalCornersFaker().generate();
        frameBuilder
                .withCorners(corners)
                .build(
                        new BlockUtil.FoundRectangle(
                                interiorBottomLeft,
                                FAKER.number().numberBetween(PortalFrame.MIN_WIDTH + 1, PortalFrame.MAX_WIDTH + 1),
                                FAKER.number().numberBetween(PortalFrame.MIN_HEIGHT + 1, PortalFrame.MAX_HEIGHT + 1)),
                        axis);

        var shape = new PortalFrame(level, interiorBottomLeft, axis, portalBlock);

        assertThat(shape.readCorners()).isEqualTo(corners);
    }

    @Test
    void readCorners_returnsNoCorners_whenFrameIsInvalid() {
        var shape = new PortalFrame(level, interiorBottomLeft, PortalFootprintFaker.generateAxis(), portalBlock);

        assertThat(shape.readCorners()).isEmpty();
    }

    @Test
    void isComplete_reportsComplete_whenCornersAreAir() {
        frameBuilder.withCornerBlock(Blocks.AIR).build(interior, Axis.X);
        new PortalFrame(level, interiorBottomLeft, Axis.X, portalBlock).createPortalBlocks(level);

        var shape = new PortalFrame(level, interiorBottomLeft, Axis.X, portalBlock);

        assertThat(shape.isComplete()).isTrue();
    }

    private List<BlockPos> listInteriorCells() {
        return BlockPos.betweenClosedStream(interiorBottomLeft, interiorBottomLeft.offset(width - 1, height - 1, 0))
                .map(BlockPos::immutable)
                .toList();
    }

    private List<BlockState> readInteriorStates() {
        return listInteriorCells().stream().map(level::getBlockState).toList();
    }

    private BlockState createPortalState(Axis axis) {
        return portalBlock.defaultBlockState().setValue(DimensionPortalBlock.AXIS, axis);
    }
}
