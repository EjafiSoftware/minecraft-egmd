package com.ejafi.egmd.portal;

import static org.assertj.core.api.Assertions.assertThat;

import com.ejafi.egmd.dimension.MiningDimension;
import com.ejafi.egmd.dimension.MiningDimensionFaker;
import net.datafaker.Faker;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class MiningPortalBlockTest {
    private static final Faker FAKER = new Faker();

    private final MiningDimension portalDimension = MiningDimensionFaker.pickRegistered();
    private final Axis portalAxis = PortalFootprintFaker.generateAxis();

    @Test
    void resolveDestination_leadsToMiningDimension_whenTravelingFromOverworld() {
        ResourceKey<Level> destination =
                MiningPortalBlock.resolveDestination(Level.OVERWORLD, portalDimension.levelKey());

        assertThat(destination).isEqualTo(portalDimension.levelKey());
    }

    @Test
    void resolveDestination_leadsToOverworld_whenTravelingFromPortalsOwnMiningDimension() {
        ResourceKey<Level> destination =
                MiningPortalBlock.resolveDestination(portalDimension.levelKey(), portalDimension.levelKey());

        assertThat(destination).isEqualTo(Level.OVERWORLD);
    }

    @Test
    void resolveDestination_leadsNowhere_whenPortalStandsInVanillaDimensionOtherThanOverworld() {
        ResourceKey<Level> destination = MiningPortalBlock.resolveDestination(
                FAKER.options().option(Level.NETHER, Level.END), portalDimension.levelKey());

        assertThat(destination).isNull();
    }

    @Test
    void resolveDestination_leadsNowhere_whenPortalStandsInAnotherMiningDimension() {
        MiningDimension other = new MiningDimensionFaker().generate();

        ResourceKey<Level> destination =
                MiningPortalBlock.resolveDestination(other.levelKey(), portalDimension.levelKey());

        assertThat(destination).isNull();
    }

    @Test
    void computeArrivalTarget_snapsTargetToFlatSurface_whenDestinationIsMiningDimension() {
        PortalFootprint scaled = new PortalFootprintFaker().generate();

        ArrivalTarget target = MiningPortalBlock.computeArrivalTarget(scaled, portalDimension.levelKey());

        assertThat(target)
                .isEqualTo(new ArrivalTarget.OnFlatSurface(
                        new PortalFootprint(scaled.origin().atY(portalDimension.computeSurfaceY()), scaled.axis())));
    }

    @Test
    void computeArrivalTarget_keepsTargetOnTerrain_whenDestinationIsOverworld() {
        PortalFootprint scaled = new PortalFootprintFaker().generate();

        ArrivalTarget target = MiningPortalBlock.computeArrivalTarget(scaled, Level.OVERWORLD);

        assertThat(target).isEqualTo(new ArrivalTarget.OnTerrain(scaled));
    }

    @Test
    void isAcrossPlane_reportsAcross_whenNeighborFacesPortalFrontOrBack() {
        Direction front = PortalFootprint.resolveAlongDirection(portalAxis).getClockWise();

        boolean across =
                MiningPortalBlock.isAcrossPlane(portalAxis, FAKER.options().option(front, front.getOpposite()));

        assertThat(across).isTrue();
    }

    @Test
    void isAcrossPlane_reportsInPlane_whenNeighborSitsBesidePortal() {
        Direction along = PortalFootprint.resolveAlongDirection(portalAxis);

        boolean across =
                MiningPortalBlock.isAcrossPlane(portalAxis, FAKER.options().option(along, along.getOpposite()));

        assertThat(across).isFalse();
    }

    @Test
    void isAcrossPlane_reportsInPlane_whenNeighborSitsAboveOrBelowPortal() {
        boolean across =
                MiningPortalBlock.isAcrossPlane(portalAxis, FAKER.options().option(Direction.UP, Direction.DOWN));

        assertThat(across).isFalse();
    }
}
