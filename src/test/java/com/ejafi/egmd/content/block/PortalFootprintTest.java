package com.ejafi.egmd.content.block;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ejafi.egmd.core.registration.ModBlocks;
import com.ejafi.egmd.core.registration.PortalVariants;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PortalFootprintTest {
    private static final BlockPos ORIGIN = new BlockPos(0, 100, 0);

    private final FakeLevel level = new FakeLevel();
    private final WorldBorder border = new WorldBorder();

    @Test
    void canHost_acceptsOrigin_whenFootprintIsOpenGround() {
        fillGroundBelowOrigin();

        assertTrue(PortalFootprint.canHost(level, border, ORIGIN, Axis.X));
    }

    static Stream<Arguments> provideBlockersOfAnOpenSite() {
        return Stream.of(
                Arguments.of("container in frame plane", new BlockPos(2, 1, 0), Blocks.CHEST, Axis.X),
                Arguments.of("container under platform", new BlockPos(0, -1, 1), Blocks.SHULKER_BOX, Axis.X),
                Arguments.of("another portal in frame plane", new BlockPos(0, 1, 0), getOverworldPortal(), Axis.X),
                Arguments.of("solid block in frame plane", new BlockPos(0, 2, 0), Blocks.STONE, Axis.X),
                Arguments.of("water in frame plane", new BlockPos(0, 1, 0), Blocks.WATER, Axis.X),
                Arguments.of("bedrock under platform", new BlockPos(-1, -1, 0), Blocks.BEDROCK, Axis.X),
                Arguments.of("dirt in exit space, X axis", new BlockPos(1, 1, 1), Blocks.DIRT, Axis.X),
                Arguments.of("dirt in exit space, Z axis", new BlockPos(-1, 0, 1), Blocks.DIRT, Axis.Z));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideBlockersOfAnOpenSite")
    void canHost_rejectsOrigin_whenBlockerSitsInFootprint(String description, BlockPos offset, Block blocker, Axis axis) {
        level.setBlock(ORIGIN.offset(offset), blocker.defaultBlockState(), 3);

        assertFalse(PortalFootprint.canHost(level, border, ORIGIN, axis));
    }

    @Test
    void canHost_rejectsOrigin_whenPlatformWouldSitOnBottomLayerOfWorld() {
        BlockPos origin = new BlockPos(0, level.getMinBuildHeight() + 1, 0);

        assertFalse(PortalFootprint.canHost(level, border, origin, Axis.X));
    }

    @Test
    void canHost_rejectsOrigin_whenTopFrameWouldExceedBuildHeight() {
        BlockPos origin = new BlockPos(0, level.getMaxBuildHeight() - MiningPortalShape.MIN_HEIGHT, 0);

        assertFalse(PortalFootprint.canHost(level, border, origin, Axis.X));
    }

    @Test
    void canHost_rejectsOrigin_whenExitWouldLandOutsideWorldBorder() {
        border.setSize(100);
        BlockPos lastBlockInsideBorder = new BlockPos(0, 100, 49);

        assertFalse(PortalFootprint.canHost(level, border, lastBlockInsideBorder, Axis.X));
    }

    @Test
    void canHost_acceptsOrigin_whenWholeFootprintFitsInsideWorldBorder() {
        border.setSize(100);
        BlockPos origin = new BlockPos(0, 100, 48);

        assertTrue(PortalFootprint.canHost(level, border, origin, Axis.X));
    }

    @Test
    void canForce_acceptsOrigin_whenFootprintIsSolidStone() {
        fillFootprintWithStone();

        assertTrue(PortalFootprint.canForce(level, border, ORIGIN, Axis.X));
    }

    @Test
    void canForce_rejectsOrigin_whenContainerSitsInExitSpace() {
        level.setBlock(ORIGIN.south(), Blocks.CHEST.defaultBlockState(), 3);

        assertFalse(PortalFootprint.canForce(level, border, ORIGIN, Axis.X));
    }

    @Test
    void canForce_rejectsOrigin_whenBedrockSitsInFramePlane() {
        level.setBlock(ORIGIN.west(), Blocks.BEDROCK.defaultBlockState(), 3);

        assertFalse(PortalFootprint.canForce(level, border, ORIGIN, Axis.X));
    }

    private void fillGroundBelowOrigin() {
        for (int x = -3; x <= 4; x++) {
            for (int z = -3; z <= 3; z++) {
                level.setBlock(ORIGIN.offset(x, -1, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            }
        }
    }

    private void fillFootprintWithStone() {
        for (int x = -1; x <= 2; x++) {
            for (int y = -1; y <= MiningPortalShape.MIN_HEIGHT; y++) {
                for (int z = -1; z <= 1; z++) {
                    level.setBlock(ORIGIN.offset(x, y, z), Blocks.STONE.defaultBlockState(), 3);
                }
            }
        }
    }

    private static MiningPortalBlock getOverworldPortal() {
        return ModBlocks.PORTAL_BLOCKS.get(PortalVariants.OVERWORLD).get();
    }
}
