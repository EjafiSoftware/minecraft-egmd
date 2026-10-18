package com.ejafi.egmd.portal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.stream.Stream;
import net.datafaker.Faker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PortalFootprintTest {
    private static final Faker FAKER = new Faker();
    private static final int BORDER_SIZE = 100;
    private static final int LOWEST_ORIGIN_X_INSIDE_BORDER = -49;
    private static final int HIGHEST_ORIGIN_X_INSIDE_BORDER = 47;
    private static final int HIGHEST_ORIGIN_Z_INSIDE_BORDER = 48;

    private final FakeLevel level = new FakeLevel();
    private final PortalFootprint footprint =
            new PortalFootprintFaker().withAxis(Axis.X).generate();

    @Test
    void canHost_acceptsOrigin_whenFootprintIsOpenGround() {
        fillGroundBelowOrigin();

        boolean hostable = PortalFootprint.canHost(level, footprint);

        assertThat(hostable).isTrue();
    }

    static Stream<Arguments> provideBlockersOfAnOpenSite() {
        return Stream.of(
                Arguments.argumentSet("container in frame plane", new BlockPos(2, 1, 0), Blocks.CHEST),
                Arguments.argumentSet("container under platform", new BlockPos(0, -1, 1), Blocks.SHULKER_BOX),
                Arguments.argumentSet(
                        "another portal in frame plane",
                        new BlockPos(0, 1, 0),
                        PortalRegisters.getPortalBlock(PortalVariants.OVERWORLD_MINING)
                                .get()),
                Arguments.argumentSet("solid block in frame plane", new BlockPos(0, 2, 0), Blocks.STONE),
                Arguments.argumentSet("water in frame plane", new BlockPos(0, 1, 0), Blocks.WATER),
                Arguments.argumentSet("bedrock under platform", new BlockPos(-1, -1, 0), Blocks.BEDROCK),
                Arguments.argumentSet("dirt in exit space", new BlockPos(1, 1, 1), Blocks.DIRT));
    }

    @ParameterizedTest
    @MethodSource("provideBlockersOfAnOpenSite")
    void canHost_rejectsOrigin_whenBlockerSitsInFootprint(BlockPos offset, Block blocker) {
        level.setBlock(footprint.origin().offset(offset), blocker.defaultBlockState(), Block.UPDATE_ALL);

        boolean hostable = PortalFootprint.canHost(level, footprint);

        assertThat(hostable).isFalse();
    }

    @Test
    void canHost_rejectsOrigin_whenDirtSitsInExitSpaceOfZAxisPortal() {
        PortalFootprint alongZ = new PortalFootprintFaker().withAxis(Axis.Z).generate();
        level.setBlock(alongZ.origin().offset(-1, 0, 1), Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);

        boolean hostable = PortalFootprint.canHost(level, alongZ);

        assertThat(hostable).isFalse();
    }

    @Test
    void canHost_rejectsOrigin_whenPlatformWouldSitOnBottomLayerOfWorld() {
        PortalFootprint bottom = new PortalFootprintFaker()
                .withOriginY(level.getMinBuildHeight() + 1)
                .generate();

        boolean hostable = PortalFootprint.canHost(level, bottom);

        assertThat(hostable).isFalse();
    }

    @Test
    void canHost_rejectsOrigin_whenTopFrameWouldExceedBuildHeight() {
        PortalFootprint top = new PortalFootprintFaker()
                .withOriginY(level.getMaxBuildHeight() - PortalFrame.MIN_HEIGHT)
                .generate();

        boolean hostable = PortalFootprint.canHost(level, top);

        assertThat(hostable).isFalse();
    }

    @Test
    void canHost_rejectsOrigin_whenExitWouldLandOutsideWorldBorder() {
        level.getWorldBorder().setSize(BORDER_SIZE);
        PortalFootprint beyondBorder = generateFootprintInsideBorderAtZ(HIGHEST_ORIGIN_Z_INSIDE_BORDER + 1);

        boolean hostable = PortalFootprint.canHost(level, beyondBorder);

        assertThat(hostable).isFalse();
    }

    @Test
    void canHost_acceptsOrigin_whenWholeFootprintFitsInsideWorldBorder() {
        level.getWorldBorder().setSize(BORDER_SIZE);
        PortalFootprint insideBorder = generateFootprintInsideBorderAtZ(HIGHEST_ORIGIN_Z_INSIDE_BORDER);

        boolean hostable = PortalFootprint.canHost(level, insideBorder);

        assertThat(hostable).isTrue();
    }

    @Test
    void canForce_acceptsOrigin_whenFootprintIsSolidStone() {
        fillFootprintWithStone();

        boolean forceable = PortalFootprint.canForce(level, footprint);

        assertThat(forceable).isTrue();
    }

    @Test
    void canForce_rejectsOrigin_whenContainerSitsInExitSpace() {
        level.setBlock(footprint.origin().south(), Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);

        boolean forceable = PortalFootprint.canForce(level, footprint);

        assertThat(forceable).isFalse();
    }

    @Test
    void canForce_rejectsOrigin_whenBedrockSitsInFramePlane() {
        level.setBlock(footprint.origin().west(), Blocks.BEDROCK.defaultBlockState(), Block.UPDATE_ALL);

        boolean forceable = PortalFootprint.canForce(level, footprint);

        assertThat(forceable).isFalse();
    }

    @Test
    void locateCorners_placesCornersDiagonalToPortal_whenAxisIsX() {
        BlockPos origin = footprint.origin();

        Map<PortalCorner, BlockPos> corners = footprint.locateCorners();

        assertThat(corners)
                .isEqualTo(Map.of(
                        PortalCorner.LOW_BOTTOM, origin.offset(-1, -1, 0),
                        PortalCorner.HIGH_BOTTOM, origin.offset(PortalFrame.MIN_WIDTH, -1, 0),
                        PortalCorner.LOW_TOP, origin.offset(-1, PortalFrame.MIN_HEIGHT, 0),
                        PortalCorner.HIGH_TOP, origin.offset(PortalFrame.MIN_WIDTH, PortalFrame.MIN_HEIGHT, 0)));
    }

    @Test
    void locateCorners_placesCornersDiagonalToPortal_whenAxisIsZ() {
        PortalFootprint alongZ = new PortalFootprintFaker().withAxis(Axis.Z).generate();
        BlockPos origin = alongZ.origin();

        Map<PortalCorner, BlockPos> corners = alongZ.locateCorners();

        assertThat(corners)
                .isEqualTo(Map.of(
                        PortalCorner.LOW_BOTTOM, origin.offset(0, -1, -1),
                        PortalCorner.HIGH_BOTTOM, origin.offset(0, -1, PortalFrame.MIN_WIDTH),
                        PortalCorner.LOW_TOP, origin.offset(0, PortalFrame.MIN_HEIGHT, -1),
                        PortalCorner.HIGH_TOP, origin.offset(0, PortalFrame.MIN_HEIGHT, PortalFrame.MIN_WIDTH)));
    }

    private PortalFootprint generateFootprintInsideBorderAtZ(int z) {
        return new PortalFootprintFaker()
                .withAxis(Axis.X)
                .withOriginX(
                        FAKER.number().numberBetween(LOWEST_ORIGIN_X_INSIDE_BORDER, HIGHEST_ORIGIN_X_INSIDE_BORDER + 1))
                .withOriginZ(z)
                .generate();
    }

    private void fillGroundBelowOrigin() {
        for (int x = -3; x <= 4; x++) {
            for (int z = -3; z <= 3; z++) {
                level.setBlock(
                        footprint.origin().offset(x, -1, z), Blocks.GRASS_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    private void fillFootprintWithStone() {
        for (int x = -1; x <= 2; x++) {
            for (int y = -1; y <= PortalFrame.MIN_HEIGHT; y++) {
                for (int z = -1; z <= 1; z++) {
                    level.setBlock(
                            footprint.origin().offset(x, y, z), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }
}
