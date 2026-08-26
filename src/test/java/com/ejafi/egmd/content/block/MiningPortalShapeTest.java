package com.ejafi.egmd.content.block;

import com.ejafi.egmd.core.registration.ModBlocks;
import com.ejafi.egmd.core.registration.PortalVariants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(EphemeralTestServerProvider.class)
class MiningPortalShapeTest {
    private static final Block FRAME_BLOCK = Blocks.COBBLESTONE;

    @Test
    void isValid_reportsValidShape_whenInteriorMeetsMinimumSize(MinecraftServer server) {
        ServerLevel level = server.overworld();
        BlockPos interiorBottomLeft = new BlockPos(0, 200, 0);
        buildFrame(level, interiorBottomLeft, MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT);

        MiningPortalShape shape = new MiningPortalShape(level, interiorBottomLeft, Axis.X, portalBlock(), FRAME_BLOCK);

        assertTrue(shape.isValid());
        assertEquals(MiningPortalShape.MIN_HEIGHT, shape.getHeight());
    }

    @Test
    void isValid_rejectsShape_whenInteriorIsShorterThanMinimumHeight(MinecraftServer server) {
        ServerLevel level = server.overworld();
        BlockPos interiorBottomLeft = new BlockPos(64, 200, 0);
        buildFrame(level, interiorBottomLeft, MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT - 1);

        MiningPortalShape shape = new MiningPortalShape(level, interiorBottomLeft, Axis.X, portalBlock(), FRAME_BLOCK);

        assertFalse(shape.isValid());
    }

    @Test
    void getHeight_measuresWholeFrame_whenStartedFromTopInteriorBlock(MinecraftServer server) {
        ServerLevel level = server.overworld();
        BlockPos interiorBottomLeft = new BlockPos(128, 200, 0);
        int interiorHeight = 4;
        buildFrame(level, interiorBottomLeft, MiningPortalShape.MIN_WIDTH, interiorHeight);
        BlockPos topInteriorBlock = interiorBottomLeft.above(interiorHeight - 1);

        MiningPortalShape shape = new MiningPortalShape(level, topInteriorBlock, Axis.X, portalBlock(), FRAME_BLOCK);

        assertTrue(shape.isValid());
        assertEquals(interiorHeight, shape.getHeight());
    }

    private static Block portalBlock() {
        return ModBlocks.PORTAL_BLOCKS.get(PortalVariants.OVERWORLD).get();
    }

    private static void buildFrame(ServerLevel level, BlockPos interiorBottomLeft, int interiorWidth, int interiorHeight) {
        for (int x = -1; x <= interiorWidth; x++) {
            setFrame(level, interiorBottomLeft.offset(x, -1, 0));
            setFrame(level, interiorBottomLeft.offset(x, interiorHeight, 0));
        }

        for (int y = 0; y < interiorHeight; y++) {
            setFrame(level, interiorBottomLeft.offset(-1, y, 0));
            setFrame(level, interiorBottomLeft.offset(interiorWidth, y, 0));

            for (int x = 0; x < interiorWidth; x++) {
                level.setBlockAndUpdate(interiorBottomLeft.offset(x, y, 0), Blocks.AIR.defaultBlockState());
            }
        }
    }

    private static void setFrame(ServerLevel level, BlockPos pos) {
        level.setBlockAndUpdate(pos, FRAME_BLOCK.defaultBlockState());
    }
}
