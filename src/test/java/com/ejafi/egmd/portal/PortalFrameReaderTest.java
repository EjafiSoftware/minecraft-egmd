package com.ejafi.egmd.portal;

import static org.assertj.core.api.Assertions.assertThat;

import com.ejafi.egmd.BlockPosFaker;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.datafaker.Faker;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

class PortalFrameReaderTest {
    private static final Faker FAKER = new Faker();

    private final FakeLevel level = new FakeLevel();
    private final DimensionPortalBlock portalBlock =
            PortalRegisters.getPortalBlock(PortalVariants.OVERWORLD_MINING).get();
    private final PortalFrameReader reader = new PortalFrameReader(level, portalBlock);
    private final PortalFrameBuilder frameBuilder = new PortalFrameBuilder(level, portalBlock.getFrameBlock());
    private final BlockPos interiorMinCorner = new BlockPosFaker()
            .withY(FAKER.number()
                    .numberBetween(
                            level.getMinBuildHeight() + 1, level.getMaxBuildHeight() - PortalFrame.MAX_HEIGHT - 2))
            .generate();
    private final int width = FAKER.number().numberBetween(PortalFrame.MIN_WIDTH, PortalFrame.MAX_WIDTH + 1);
    private final int height = FAKER.number().numberBetween(PortalFrame.MIN_HEIGHT, PortalFrame.MAX_HEIGHT + 1);
    private final PortalFrame frame = createFrameAlongX(width, height);

    @Test
    void find_findsFrame_whenInteriorMeetsMinimumSize() {
        PortalFrame smallest = createFrameAlongX(PortalFrame.MIN_WIDTH, PortalFrame.MIN_HEIGHT);
        frameBuilder.build(smallest);

        Optional<PortalFrame> found = reader.find(interiorMinCorner, Axis.X);

        assertThat(found).usingRecursiveComparison().isEqualTo(Optional.of(smallest));
    }

    @Test
    void find_findsFrame_whenFrameIsBuiltAlongZAxis() {
        var alongZ = new PortalFrame(new BlockUtil.FoundRectangle(interiorMinCorner, width, height), Axis.Z);
        frameBuilder.build(alongZ);

        Optional<PortalFrame> found = reader.find(interiorMinCorner, Axis.Z);

        assertThat(found).usingRecursiveComparison().isEqualTo(Optional.of(alongZ));
    }

    @Test
    void find_findsWholeFrame_whenStartedFromAnyInteriorCell() {
        frameBuilder.build(frame);

        Optional<PortalFrame> found = reader.find(FAKER.options().nextElement(listInteriorCells()), Axis.X);

        assertThat(found).usingRecursiveComparison().isEqualTo(Optional.of(frame));
    }

    @Test
    void find_findsNoFrame_whenInteriorIsShorterThanMinimumHeight() {
        frameBuilder.build(createFrameAlongX(width, PortalFrame.MIN_HEIGHT - 1));

        Optional<PortalFrame> found = reader.find(interiorMinCorner, Axis.X);

        assertThat(found).isEmpty();
    }

    @Test
    void find_findsNoFrame_whenInteriorIsNarrowerThanMinimumWidth() {
        frameBuilder.build(createFrameAlongX(PortalFrame.MIN_WIDTH - 1, height));

        Optional<PortalFrame> found = reader.find(interiorMinCorner, Axis.X);

        assertThat(found).isEmpty();
    }

    @Test
    void find_findsFrame_whenInteriorIsMaximumWidth() {
        PortalFrame widest = createFrameAlongX(PortalFrame.MAX_WIDTH, height);
        frameBuilder.build(widest);

        Optional<PortalFrame> found = reader.find(interiorMinCorner, Axis.X);

        assertThat(found).usingRecursiveComparison().isEqualTo(Optional.of(widest));
    }

    @Test
    void find_findsNoFrame_whenInteriorIsWiderThanMaximumWidth() {
        frameBuilder.build(createFrameAlongX(PortalFrame.MAX_WIDTH + 1, height));

        Optional<PortalFrame> found = reader.find(interiorMinCorner, Axis.X);

        assertThat(found).isEmpty();
    }

    @Test
    void find_findsFrame_whenInteriorIsMaximumHeight() {
        PortalFrame tallest = createFrameAlongX(width, PortalFrame.MAX_HEIGHT);
        frameBuilder.build(tallest);

        Optional<PortalFrame> found = reader.find(interiorMinCorner, Axis.X);

        assertThat(found).usingRecursiveComparison().isEqualTo(Optional.of(tallest));
    }

    @Test
    void find_findsNoFrame_whenInteriorIsTallerThanMaximumHeight() {
        frameBuilder.build(createFrameAlongX(width, PortalFrame.MAX_HEIGHT + 1));

        Optional<PortalFrame> found = reader.find(interiorMinCorner, Axis.X);

        assertThat(found).isEmpty();
    }

    @Test
    void find_findsNoFrame_whenTopFrameBlockIsMissing() {
        frameBuilder.build(frame);
        level.removeBlock(interiorMinCorner.above(height), false);

        Optional<PortalFrame> found = reader.find(interiorMinCorner, Axis.X);

        assertThat(found).isEmpty();
    }

    @Test
    void find_findsFrame_whenFireBurnsInsideTheFrame() {
        frameBuilder.build(frame);
        level.setBlock(
                FAKER.options().nextElement(listInteriorCells()), Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);

        Optional<PortalFrame> found = reader.find(interiorMinCorner, Axis.X);

        assertThat(found).usingRecursiveComparison().isEqualTo(Optional.of(frame));
    }

    @Test
    void find_findsFrame_whenSilverfishInfestsFrameBlocks() {
        frameBuilder.build(frame);
        BlockState infested = Blocks.INFESTED_COBBLESTONE.defaultBlockState();
        level.setBlock(
                interiorMinCorner.west().above(FAKER.number().numberBetween(0, height)), infested, Block.UPDATE_ALL);
        level.setBlock(interiorMinCorner.above(height), infested, Block.UPDATE_ALL);

        Optional<PortalFrame> found = reader.find(interiorMinCorner, Axis.X);

        assertThat(found).usingRecursiveComparison().isEqualTo(Optional.of(frame));
    }

    @Test
    void find_findsFrame_whenCornersAreAir() {
        frameBuilder.withCornerBlock(Blocks.AIR).build(frame);

        Optional<PortalFrame> found = reader.find(interiorMinCorner, Axis.X);

        assertThat(found).usingRecursiveComparison().isEqualTo(Optional.of(frame));
    }

    @Test
    void isComplete_reportsComplete_whenInteriorIsFullyFilledWithPortalBlocks() {
        frameBuilder.build(frame);
        portalBlock.fill(level, frame);

        boolean complete = reader.isComplete(frame);

        assertThat(complete).isTrue();
    }

    @Test
    void isComplete_reportsIncomplete_whenOneInteriorPortalBlockIsMissing() {
        frameBuilder.build(frame);
        portalBlock.fill(level, frame);
        level.removeBlock(FAKER.options().nextElement(listInteriorCells()), false);

        boolean complete = reader.isComplete(frame);

        assertThat(complete).isFalse();
    }

    @Test
    void isEmpty_reportsOccupied_whenInteriorAlreadyHoldsPortalBlocks() {
        frameBuilder.build(frame);
        level.setBlock(
                FAKER.options().nextElement(listInteriorCells()),
                portalBlock
                        .defaultBlockState()
                        .setValue(DimensionPortalBlock.AXIS, PortalFootprintFaker.generateAxis()),
                Block.UPDATE_ALL);

        boolean empty = reader.isEmpty(frame);

        assertThat(empty).isFalse();
    }

    @Test
    void isEmpty_reportsEmpty_whenInteriorIsAir() {
        frameBuilder.build(frame);

        boolean empty = reader.isEmpty(frame);

        assertThat(empty).isTrue();
    }

    @Test
    void isFrame_matchesBlock_whenItIsTheInfestedVariantOfTheFrameBlock() {
        boolean matches =
                PortalFrameReader.isFrame(Blocks.INFESTED_COBBLESTONE.defaultBlockState(), Blocks.COBBLESTONE);

        assertThat(matches).isTrue();
    }

    @Test
    void isFrame_rejectsBlock_whenItIsTheInfestedVariantOfAnotherBlock() {
        boolean matches = PortalFrameReader.isFrame(Blocks.INFESTED_STONE.defaultBlockState(), Blocks.COBBLESTONE);

        assertThat(matches).isFalse();
    }

    @Test
    void resolveInfestedHost_returnsHostBlock_whenStateIsInfested() {
        Block host = PortalFrameReader.resolveInfestedHost(Blocks.INFESTED_COBBLESTONE.defaultBlockState());

        assertThat(host).isEqualTo(Blocks.COBBLESTONE);
    }

    @Test
    void resolveInfestedHost_returnsBlockItself_whenStateIsNotInfested() {
        Block block = FAKER.options().option(Blocks.COBBLESTONE, Blocks.NETHERRACK, Blocks.END_STONE);

        Block host = PortalFrameReader.resolveInfestedHost(block.defaultBlockState());

        assertThat(host).isEqualTo(block);
    }

    @Test
    void readCorners_returnsStatesDiagonalToInterior_whenFrameIsMinimumSizeAlongXAxis() {
        Map<PortalCorner, BlockState> corners = new PortalCornersFaker().generate();
        PortalFrame smallest = createFrameAlongX(PortalFrame.MIN_WIDTH, PortalFrame.MIN_HEIGHT);
        frameBuilder.withCorners(corners).build(smallest);

        Map<PortalCorner, BlockState> read = reader.readCorners(smallest);

        assertThat(read).isEqualTo(corners);
    }

    @Test
    void readCorners_returnsStatesDiagonalToInterior_whenFrameIsMinimumSizeAlongZAxis() {
        Map<PortalCorner, BlockState> corners = new PortalCornersFaker().generate();
        var smallest = new PortalFrame(
                new BlockUtil.FoundRectangle(interiorMinCorner, PortalFrame.MIN_WIDTH, PortalFrame.MIN_HEIGHT), Axis.Z);
        frameBuilder.withCorners(corners).build(smallest);

        Map<PortalCorner, BlockState> read = reader.readCorners(smallest);

        assertThat(read).isEqualTo(corners);
    }

    @Test
    void readCorners_returnsStatesDiagonalToInterior_whenFrameIsLargerThanMinimum() {
        Map<PortalCorner, BlockState> corners = new PortalCornersFaker().generate();
        var larger = new PortalFrame(
                new BlockUtil.FoundRectangle(
                        interiorMinCorner,
                        FAKER.number().numberBetween(PortalFrame.MIN_WIDTH + 1, PortalFrame.MAX_WIDTH + 1),
                        FAKER.number().numberBetween(PortalFrame.MIN_HEIGHT + 1, PortalFrame.MAX_HEIGHT + 1)),
                PortalFootprintFaker.generateAxis());
        frameBuilder.withCorners(corners).build(larger);

        Map<PortalCorner, BlockState> read = reader.readCorners(larger);

        assertThat(read).isEqualTo(corners);
    }

    private PortalFrame createFrameAlongX(int interiorWidth, int interiorHeight) {
        return new PortalFrame(new BlockUtil.FoundRectangle(interiorMinCorner, interiorWidth, interiorHeight), Axis.X);
    }

    private List<BlockPos> listInteriorCells() {
        return BlockPos.betweenClosedStream(interiorMinCorner, interiorMinCorner.offset(width - 1, height - 1, 0))
                .map(BlockPos::immutable)
                .toList();
    }
}
