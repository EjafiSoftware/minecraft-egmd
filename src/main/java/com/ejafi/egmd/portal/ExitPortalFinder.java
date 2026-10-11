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
    private final PortalVariant variant;
    private final ResourceKey<PoiType> poiTypeKey;

    ExitPortalFinder(ServerLevel level, PortalVariant variant) {
        this.level = level;
        this.variant = variant;
        this.poiTypeKey = PortalRegisters.getPoiTypeKey(variant);
    }

    Optional<BlockUtil.FoundRectangle> findOrCreate(ArrivalTarget target, Map<PortalCorner, BlockState> corners) {
        return findPortalAround(target.footprint().origin())
                .map(this::findRectangleAround)
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
                .filter(poiPos -> level.getBlockState(poiPos).hasProperty(MiningPortalBlock.AXIS))
                .min(Comparator.<BlockPos>comparingDouble(poiPos -> poiPos.distSqr(pos))
                        .thenComparingInt(BlockPos::getY));
    }

    private int computeSearchRadius() {
        return Math.max(PortalConfig.MIN_PORTAL_SEARCH_RADIUS, (int) (PortalConfig.PORTAL_SEARCH_RADIUS.getAsInt()
                / level.dimensionType().coordinateScale()));
    }

    private BlockUtil.FoundRectangle findRectangleAround(BlockPos portalPos) {
        BlockState state = level.getBlockState(portalPos);
        return BlockUtil.getLargestRectangleAround(
                portalPos,
                state.getValue(MiningPortalBlock.AXIS),
                MiningPortalShape.MAX_WIDTH,
                Axis.Y,
                MiningPortalShape.MAX_HEIGHT,
                pos -> level.getBlockState(pos).equals(state));
    }

    private Optional<BlockUtil.FoundRectangle> createPortal(
            ArrivalTarget target, Map<PortalCorner, BlockState> corners) {
        var builder = new PortalStructureBuilder(
                PortalRegisters.getPortalBlock(variant).get(), corners);
        return PortalPositionFinder.find(level, target).map(site -> {
            builder.build(level, site);
            return new BlockUtil.FoundRectangle(
                    site.footprint().origin(), MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT);
        });
    }
}
