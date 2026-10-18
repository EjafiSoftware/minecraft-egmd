package com.ejafi.egmd.portal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

class PortalStructureBuilderTest {
    private final FakeLevel level = new FakeLevel();
    private final DimensionPortalBlock portalBlock =
            PortalRegisters.getPortalBlock(PortalVariants.OVERWORLD_MINING).get();
    private final Map<PortalCorner, BlockState> corners = new PortalCornersFaker().generate();
    private final PortalStructureBuilder builder = new PortalStructureBuilder(portalBlock, corners);

    @Test
    void build_placesCornerStatesAtFootprintCorners_whenAxisIsX() {
        PortalFootprint footprint = new PortalFootprintFaker().withAxis(Axis.X).generate();

        builder.build(level, new PortalSite.Open(footprint));

        assertThat(readCorners(footprint)).isEqualTo(corners);
    }

    @Test
    void build_placesCornerStatesAtFootprintCorners_whenAxisIsZ() {
        PortalFootprint footprint = new PortalFootprintFaker().withAxis(Axis.Z).generate();

        builder.build(level, new PortalSite.Open(footprint));

        assertThat(readCorners(footprint)).isEqualTo(corners);
    }

    @Test
    void build_placesFrameBlocksAroundPortal_whenCellIsNotACorner() {
        PortalFootprint footprint = new PortalFootprintFaker().generate();

        builder.build(level, new PortalSite.Open(footprint));

        Collection<BlockPos> cornerCells = footprint.locateCorners().values();
        List<BlockState> nonCornerStates = Stream.concat(
                        footprint.listFrameCells().stream(), footprint.listPlatformCells().stream())
                .filter(pos -> !cornerCells.contains(pos))
                .map(level::getBlockState)
                .toList();
        assertThat(nonCornerStates).containsOnly(portalBlock.getFrameBlock().defaultBlockState());
    }

    private Map<PortalCorner, BlockState> readCorners(PortalFootprint footprint) {
        return new PortalFrame(level, footprint.origin(), footprint.axis(), portalBlock).readCorners();
    }
}
