package com.ejafi.egmd.portal;

import java.util.stream.Stream;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

final class PortalStructureBuilder {
    private final MiningPortalBlock portal;
    private final BlockState frame;

    PortalStructureBuilder(MiningPortalBlock portal) {
        this.portal = portal;
        this.frame = portal.getFrameBlock().defaultBlockState();
    }

    void build(LevelWriter level, PortalSite site) {
        switch (site) {
            case PortalSite.Open(PortalFootprint footprint) -> place(level, footprint);
            case PortalSite.Forced(PortalFootprint footprint) -> {
                clearExit(level, footprint);
                place(level, footprint);
            }
        }
    }

    private void place(LevelWriter level, PortalFootprint footprint) {
        Stream.concat(footprint.listFrameCells().stream(), footprint.listPlatformCells().stream())
                .forEach(pos -> level.setBlock(pos, frame, Block.UPDATE_ALL));

        BlockState portalState = portal.defaultBlockState().setValue(MiningPortalBlock.AXIS, footprint.axis());
        footprint
                .listPortalCells()
                .forEach(pos -> level.setBlock(pos, portalState, MiningPortalShape.PORTAL_UPDATE_FLAGS));
    }

    private static void clearExit(LevelWriter level, PortalFootprint footprint) {
        footprint.listExitCells().forEach(pos -> level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL));
    }
}
