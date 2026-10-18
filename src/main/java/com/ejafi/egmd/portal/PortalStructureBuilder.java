package com.ejafi.egmd.portal;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

final class PortalStructureBuilder {
    private final DimensionPortalBlock portal;
    private final BlockState frame;
    private final Map<PortalCorner, BlockState> corners;

    PortalStructureBuilder(DimensionPortalBlock portal, Map<PortalCorner, BlockState> corners) {
        this.portal = portal;
        this.frame = portal.getFrameBlock().defaultBlockState();
        this.corners = Map.copyOf(corners);
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
        Map<BlockPos, BlockState> cornerStates = footprint.locateCorners().entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getValue, entry -> Objects.requireNonNull(corners.get(entry.getKey()))));
        Stream.concat(footprint.listFrameCells().stream(), footprint.listPlatformCells().stream())
                .forEach(pos -> level.setBlock(pos, cornerStates.getOrDefault(pos, frame), Block.UPDATE_ALL));
        portal.fill(level, footprint.locateFrame());
    }

    private static void clearExit(LevelWriter level, PortalFootprint footprint) {
        footprint.listExitCells().forEach(pos -> level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL));
    }
}
