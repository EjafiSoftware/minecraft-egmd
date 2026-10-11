package com.ejafi.egmd.portal;

import static org.assertj.core.api.Assertions.assertThat;

import com.ejafi.egmd.BlockPosFaker;
import java.util.Optional;
import java.util.OptionalLong;
import net.datafaker.Faker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import org.junit.jupiter.api.Test;

class PortalPositionFinderTest {
    private static final Faker FAKER = new Faker();
    private static final int BORDER_SIZE = 100;
    private static final int LOWEST_ORIGIN_COORDINATE = -49;
    private static final int HIGHEST_ORIGIN_COORDINATE = 47;
    private static final int MINIMUM_FORCED_Y = 70;
    private static final DimensionType NETHER_LIKE = createDimensionType(0, 128);
    private static final int NETHER_LIKE_HIGHEST_FORCED_Y = 118;

    private final WorldBorder border = new WorldBorder();
    private final FakeLevel level = new FakeLevel();

    @Test
    void clampInsideBorder_pullsTargetInsideBorderMargin_whenTargetLiesOutsideBorder() {
        border.setSize(BORDER_SIZE);
        BlockPos target = new BlockPosFaker()
                .withX(FAKER.number().numberBetween(HIGHEST_ORIGIN_COORDINATE + 1, BlockPosFaker.HORIZONTAL_LIMIT))
                .withZ(FAKER.number().numberBetween(-BlockPosFaker.HORIZONTAL_LIMIT, LOWEST_ORIGIN_COORDINATE))
                .generate();

        BlockPos clamped = PortalPositionFinder.clampInsideBorder(border, target);

        assertThat(clamped).isEqualTo(new BlockPos(HIGHEST_ORIGIN_COORDINATE, target.getY(), LOWEST_ORIGIN_COORDINATE));
    }

    @Test
    void clampInsideBorder_keepsTarget_whenTargetLiesInsideBorderMargin() {
        border.setSize(BORDER_SIZE);
        BlockPos target = new BlockPosFaker()
                .withX(FAKER.number().numberBetween(LOWEST_ORIGIN_COORDINATE, HIGHEST_ORIGIN_COORDINATE + 1))
                .withZ(FAKER.number().numberBetween(LOWEST_ORIGIN_COORDINATE, HIGHEST_ORIGIN_COORDINATE + 1))
                .generate();

        BlockPos clamped = PortalPositionFinder.clampInsideBorder(border, target);

        assertThat(clamped).isEqualTo(target);
    }

    @Test
    void clampInsideBorder_keepsTarget_whenBorderIsNarrowerThanFootprint() {
        border.setSize(MiningPortalShape.MIN_WIDTH);
        BlockPos target = new BlockPosFaker().generate();

        BlockPos clamped = PortalPositionFinder.clampInsideBorder(border, target);

        assertThat(clamped).isEqualTo(target);
    }

    @Test
    void resolveForcedFootprint_raisesOriginToMinimumForcedY_whenTargetIsBelowIt() {
        PortalFootprint target = new PortalFootprintFaker()
                .withOriginY(FAKER.number().numberBetween(NETHER_LIKE.minY(), MINIMUM_FORCED_Y))
                .generate();

        Optional<PortalFootprint> forced = PortalPositionFinder.resolveForcedFootprint(NETHER_LIKE, target);

        assertThat(forced).contains(new PortalFootprint(target.origin().atY(MINIMUM_FORCED_Y), target.axis()));
    }

    @Test
    void resolveForcedFootprint_lowersOriginBelowCeilingMargin_whenTargetIsNearLogicalTop() {
        PortalFootprint target = new PortalFootprintFaker()
                .withOriginY(FAKER.number().numberBetween(NETHER_LIKE_HIGHEST_FORCED_Y + 1, NETHER_LIKE.height()))
                .generate();

        Optional<PortalFootprint> forced = PortalPositionFinder.resolveForcedFootprint(NETHER_LIKE, target);

        assertThat(forced)
                .contains(new PortalFootprint(target.origin().atY(NETHER_LIKE_HIGHEST_FORCED_Y), target.axis()));
    }

    @Test
    void resolveForcedFootprint_keepsTarget_whenTargetLiesWithinForcedBand() {
        PortalFootprint target = new PortalFootprintFaker()
                .withOriginY(FAKER.number().numberBetween(MINIMUM_FORCED_Y, NETHER_LIKE_HIGHEST_FORCED_Y + 1))
                .generate();

        Optional<PortalFootprint> forced = PortalPositionFinder.resolveForcedFootprint(NETHER_LIKE, target);

        assertThat(forced).contains(target);
    }

    @Test
    void resolveForcedFootprint_raisesOriginAboveWorldBottom_whenWorldBottomIsAboveMinimumForcedY() {
        DimensionType raisedBottom = createDimensionType(96, 256);
        PortalFootprint target = new PortalFootprintFaker()
                .withOriginY(FAKER.number().numberBetween(BlockPosFaker.MIN_BUILD_Y, raisedBottom.minY() + 1))
                .generate();

        Optional<PortalFootprint> forced = PortalPositionFinder.resolveForcedFootprint(raisedBottom, target);

        assertThat(forced).contains(new PortalFootprint(target.origin().atY(raisedBottom.minY() + 1), target.axis()));
    }

    @Test
    void resolveForcedFootprint_refusesSite_whenLogicalHeightLeavesNoRoomAboveMinimumForcedY() {
        DimensionType lowCeiling = createDimensionType(0, 64);
        PortalFootprint target = new PortalFootprintFaker()
                .withOriginY(FAKER.number().numberBetween(lowCeiling.minY(), lowCeiling.logicalHeight()))
                .generate();

        Optional<PortalFootprint> forced = PortalPositionFinder.resolveForcedFootprint(lowCeiling, target);

        assertThat(forced).isEmpty();
    }

    @Test
    void searchSpiralAtFixedY_returnsCenter_whenCenterCanHostPortal() {
        PortalFootprint center = new PortalFootprintFaker().generate();

        Optional<PortalFootprint> site = PortalPositionFinder.searchSpiralAtFixedY(level, center);

        assertThat(site).contains(center);
    }

    @Test
    void searchSpiralAtFixedY_returnsFirstOpenSpiralColumn_whenStoneBlocksCenter() {
        PortalFootprint center = new PortalFootprintFaker().withAxis(Axis.X).generate();
        level.setBlock(center.origin(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        Optional<PortalFootprint> site = PortalPositionFinder.searchSpiralAtFixedY(level, center);

        assertThat(site).contains(new PortalFootprint(center.origin().east().south(), Axis.X));
    }

    @Test
    void searchSpiralAtFixedY_findsNoSite_whenPlatformWouldSitOnBottomLayerOfWorld() {
        PortalFootprint center = new PortalFootprintFaker()
                .withOriginY(level.getMinBuildHeight() + 1)
                .generate();

        Optional<PortalFootprint> site = PortalPositionFinder.searchSpiralAtFixedY(level, center);

        assertThat(site).isEmpty();
    }

    private static DimensionType createDimensionType(int minY, int logicalHeight) {
        return new DimensionType(
                OptionalLong.empty(),
                true,
                false,
                false,
                true,
                1.0,
                false,
                false,
                minY,
                256,
                logicalHeight,
                BlockTags.INFINIBURN_OVERWORLD,
                BuiltinDimensionTypes.OVERWORLD_EFFECTS,
                0.0F,
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0));
    }
}
