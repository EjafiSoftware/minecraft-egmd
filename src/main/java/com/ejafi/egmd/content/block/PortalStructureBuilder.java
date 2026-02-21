package com.ejafi.egmd.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Places the frame, portal blocks, and landing platform that make up a portal structure.
 */
final class PortalStructureBuilder {
    private PortalStructureBuilder() {}

    static void build(ServerLevel level, BlockPos origin, Axis axis, MiningPortalBlock block) {
        Direction dir = axis == Axis.X ? Direction.WEST : Direction.SOUTH;
        BlockState frame = block.getFrameBlock().defaultBlockState();
        BlockState portal = block.defaultBlockState().setValue(MiningPortalBlock.AXIS, axis);

        placeFrame(level, origin, dir, frame);
        placePortalBlocks(level, origin, dir, portal);
        placePlatform(level, origin, dir, frame);
    }

    private static void placeFrame(ServerLevel level, BlockPos origin, Direction dir, BlockState frame) {
        for (int x = -1; x < MiningPortalShape.MIN_WIDTH + 1; ++x) {
            for (int y = -1; y < MiningPortalShape.MIN_HEIGHT + 1; ++y) {
                if (x == -1 || x == MiningPortalShape.MIN_WIDTH || y == -1 || y == MiningPortalShape.MIN_HEIGHT) {
                    level.setBlock(origin.relative(dir, x).above(y), frame, 3);
                }
            }
        }
    }

    private static void placePortalBlocks(ServerLevel level, BlockPos origin, Direction dir, BlockState portal) {
        for (int x = 0; x < MiningPortalShape.MIN_WIDTH; ++x) {
            for (int y = 0; y < MiningPortalShape.MIN_HEIGHT; ++y) {
                level.setBlock(origin.relative(dir, x).above(y), portal, 18);
            }
        }
    }

    private static void placePlatform(ServerLevel level, BlockPos origin, Direction dir, BlockState frame) {
        Direction cw = dir.getClockWise();
        for (int x = -1; x < MiningPortalShape.MIN_WIDTH + 1; ++x) {
            for (int z = -1; z < MiningPortalShape.PLATFORM_DEPTH - 1; ++z) {
                level.setBlock(origin.relative(dir, x).relative(cw, z).below(), frame, 3);
            }
        }
    }
}
