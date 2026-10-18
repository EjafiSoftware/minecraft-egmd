package com.ejafi.egmd.portal;

import static org.assertj.core.api.Assertions.assertThat;

import com.ejafi.egmd.BlockPosFaker;
import com.ejafi.egmd.dimension.EgmdDimension;
import com.ejafi.egmd.dimension.EgmdDimensionFaker;
import com.ejafi.egmd.dimension.Terrain;
import java.util.List;
import java.util.Optional;
import net.datafaker.Faker;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

class DimensionPortalBlockTest {
    private static final Faker FAKER = new Faker();

    private final EgmdDimension portalDimension = EgmdDimensionFaker.pickRegistered();
    private final Axis portalAxis = PortalFootprintFaker.generateAxis();

    @Test
    void resolveDestination_leadsToEgmdDimension_whenTravelingFromOverworld() {
        Optional<ResourceKey<Level>> destination =
                DimensionPortalBlock.resolveDestination(Level.OVERWORLD, portalDimension.levelKey());

        assertThat(destination).contains(portalDimension.levelKey());
    }

    @Test
    void resolveDestination_leadsToOverworld_whenTravelingFromPortalsOwnDimension() {
        Optional<ResourceKey<Level>> destination =
                DimensionPortalBlock.resolveDestination(portalDimension.levelKey(), portalDimension.levelKey());

        assertThat(destination).contains(Level.OVERWORLD);
    }

    @Test
    void resolveDestination_leadsNowhere_whenPortalStandsInVanillaDimensionOtherThanOverworld() {
        Optional<ResourceKey<Level>> destination = DimensionPortalBlock.resolveDestination(
                FAKER.options().option(Level.NETHER, Level.END), portalDimension.levelKey());

        assertThat(destination).isEmpty();
    }

    @Test
    void resolveDestination_leadsNowhere_whenPortalStandsInAnotherEgmdDimension() {
        EgmdDimension other = new EgmdDimensionFaker().generate();

        Optional<ResourceKey<Level>> destination =
                DimensionPortalBlock.resolveDestination(other.levelKey(), portalDimension.levelKey());

        assertThat(destination).isEmpty();
    }

    @Test
    void computeArrivalTarget_snapsTargetToFlatSurface_whenDestinationTerrainIsFlat() {
        PortalFootprint scaled = new PortalFootprintFaker().generate();
        EgmdDimension destination =
                EgmdDimensionFaker.pickRegistered(dimension -> dimension.terrain() instanceof Terrain.Flat);

        ArrivalTarget target = DimensionPortalBlock.computeArrivalTarget(scaled, destination.levelKey());

        assertThat(destination.terrain())
                .isInstanceOfSatisfying(
                        Terrain.Flat.class,
                        terrain -> assertThat(target)
                                .isEqualTo(new ArrivalTarget.OnFlatSurface(new PortalFootprint(
                                        scaled.origin().atY(terrain.computeSurfaceY()), scaled.axis()))));
    }

    @Test
    void computeArrivalTarget_keepsTargetOnTerrain_whenDestinationTerrainIsGenerated() {
        PortalFootprint scaled = new PortalFootprintFaker().generate();
        EgmdDimension destination =
                EgmdDimensionFaker.pickRegistered(dimension -> dimension.terrain() instanceof Terrain.Generated);

        ArrivalTarget target = DimensionPortalBlock.computeArrivalTarget(scaled, destination.levelKey());

        assertThat(target).isEqualTo(new ArrivalTarget.OnTerrain(scaled));
    }

    @Test
    void computeArrivalTarget_keepsTargetOnTerrain_whenDestinationIsOverworld() {
        PortalFootprint scaled = new PortalFootprintFaker().generate();

        ArrivalTarget target = DimensionPortalBlock.computeArrivalTarget(scaled, Level.OVERWORLD);

        assertThat(target).isEqualTo(new ArrivalTarget.OnTerrain(scaled));
    }

    @Test
    void fill_placesPortalBlocksAlignedToAxisThroughoutInterior_whenAxisIsX() {
        var level = new FakeLevel();
        DimensionPortalBlock portalBlock =
                PortalRegisters.getPortalBlock(PortalVariants.OVERWORLD_MINING).get();
        BlockPos minCorner = new BlockPosFaker().generate();
        int width = FAKER.number().numberBetween(PortalFrame.MIN_WIDTH, PortalFrame.MAX_WIDTH + 1);
        int height = FAKER.number().numberBetween(PortalFrame.MIN_HEIGHT, PortalFrame.MAX_HEIGHT + 1);

        portalBlock.fill(level, new PortalFrame(new BlockUtil.FoundRectangle(minCorner, width, height), Axis.X));

        List<BlockState> interior = BlockPos.betweenClosedStream(minCorner, minCorner.offset(width - 1, height - 1, 0))
                .map(level::getBlockState)
                .toList();
        assertThat(interior).containsOnly(portalBlock.defaultBlockState().setValue(DimensionPortalBlock.AXIS, Axis.X));
    }

    @Test
    void isAcrossPlane_reportsAcross_whenNeighborFacesPortalFrontOrBack() {
        Direction front = PortalFrame.resolveAlongDirection(portalAxis).getClockWise();

        boolean across =
                DimensionPortalBlock.isAcrossPlane(portalAxis, FAKER.options().option(front, front.getOpposite()));

        assertThat(across).isTrue();
    }

    @Test
    void isAcrossPlane_reportsInPlane_whenNeighborSitsBesidePortal() {
        Direction along = PortalFrame.resolveAlongDirection(portalAxis);

        boolean across =
                DimensionPortalBlock.isAcrossPlane(portalAxis, FAKER.options().option(along, along.getOpposite()));

        assertThat(across).isFalse();
    }

    @Test
    void isAcrossPlane_reportsInPlane_whenNeighborSitsAboveOrBelowPortal() {
        boolean across =
                DimensionPortalBlock.isAcrossPlane(portalAxis, FAKER.options().option(Direction.UP, Direction.DOWN));

        assertThat(across).isFalse();
    }
}
