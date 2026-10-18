package com.ejafi.egmd.portal;

import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

final class PortalCornerMaterials {
    private final Predicate<BlockState> accepted;
    private final BlockState fallback;

    PortalCornerMaterials(Predicate<BlockState> accepted, Block fallback) {
        this.accepted = accepted;
        this.fallback = fallback.defaultBlockState();
    }

    static PortalCornerMaterials create(PortalVariant variant) {
        return new PortalCornerMaterials(state -> state.is(variant.getCornerTag()), variant.defaultCornerBlock());
    }

    boolean acceptsAll(Map<PortalCorner, BlockState> corners) {
        return corners.size() == PortalCorner.values().length
                && corners.values().stream()
                        .map(PortalCornerMaterials::resolveHost)
                        .allMatch(accepted);
    }

    Map<PortalCorner, BlockState> copyAll(Map<PortalCorner, BlockState> source) {
        return PortalCorner.mapAll(corner -> copy(corner, source.get(corner)));
    }

    private BlockState copy(PortalCorner corner, @Nullable BlockState source) {
        if (source == null) {
            return fallback;
        }

        BlockState host = resolveHost(source);
        if (!accepted.test(host)) {
            return fallback;
        }

        if (corner.isBottom() && host.getBlock() instanceof FallingBlock) {
            return fallback;
        }

        return host;
    }

    private static BlockState resolveHost(BlockState state) {
        return PortalFrameReader.resolveInfestedHost(state).defaultBlockState();
    }
}
