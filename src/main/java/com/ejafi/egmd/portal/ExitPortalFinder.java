package com.ejafi.egmd.portal;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;

final class ExitPortalFinder {
    private final ServerLevel level;
    private final ResourceKey<PoiType> poiTypeKey;
    private final DimensionPortalBlock portalBlock;

    ExitPortalFinder(ServerLevel level, PortalVariant variant) {
        this.level = level;
        this.poiTypeKey = PortalRegisters.getPoiTypeKey(variant);
        this.portalBlock = PortalRegisters.getPortalBlock(variant).get();
    }

    Optional<PortalFrame> findOrCreate(ArrivalTarget target, Map<PortalCorner, BlockState> corners) {
        return findPortalAround(target.footprint().origin())
                .map(this::findFrameAround)
                .or(() -> createPortal(target, corners));
    }

    Optional<BlockPos> findPortalAround(BlockPos pos) {
        int radius = computeSearchRadius();
        PoiManager poiManager = level.getPoiManager();
        poiManager.ensureLoadedAndValid(level, pos, radius);
        return poiManager
                .getInSquare(holder -> holder.is(poiTypeKey), pos, radius, PoiManager.Occupancy.ANY)
                .map(PoiRecord::getPos)
                .filter(level.getWorldBorder()::isWithinBounds)
                .filter(poiPos -> level.getBlockState(poiPos).is(portalBlock))
                .min(Comparator.<BlockPos>comparingDouble(poiPos -> poiPos.distSqr(pos))
                        .thenComparingInt(BlockPos::getY));
    }

    private int computeSearchRadius() {
        return Math.max(PortalConfig.MIN_PORTAL_SEARCH_RADIUS, (int) (PortalConfig.PORTAL_SEARCH_RADIUS.getAsInt()
                / level.dimensionType().coordinateScale()));
    }

    private PortalFrame findFrameAround(BlockPos portalPos) {
        BlockState state = level.getBlockState(portalPos);
        Axis axis = state.getValue(DimensionPortalBlock.AXIS);
        BlockUtil.FoundRectangle interior = BlockUtil.getLargestRectangleAround(
                portalPos,
                axis,
                PortalFrame.MAX_WIDTH,
                Axis.Y,
                PortalFrame.MAX_HEIGHT,
                pos -> level.getBlockState(pos).equals(state));
        return new PortalFrame(interior, axis);
    }

    private Optional<PortalFrame> createPortal(ArrivalTarget target, Map<PortalCorner, BlockState> corners) {
        var builder = new PortalStructureBuilder(portalBlock, corners);
        return PortalPositionFinder.find(level, target).map(site -> {
            builder.build(level, site);
            return site.footprint().locateFrame();
        });
    }
}
