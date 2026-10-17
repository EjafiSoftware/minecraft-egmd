package com.ejafi.egmd.dimension;

import static org.assertj.core.api.Assertions.assertThat;

import com.ejafi.egmd.BlockPosFaker;
import net.datafaker.Faker;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class PlayerRelocatorTest {
    private static final Faker FAKER = new Faker();

    private final ResourceKey<Level> flat = EgmdDimensionFaker.pickRegistered(
                    dimension -> dimension.terrain() instanceof Terrain.Flat)
            .levelKey();

    @Test
    void chooseRescue_leavesPlayerInPlace_whenDimensionIsVanilla() {
        ResourceKey<Level> vanilla = FAKER.options().option(Level.OVERWORLD, Level.NETHER, Level.END);

        PlayerRelocator.LoginRescue rescue =
                PlayerRelocator.chooseRescue(vanilla, PlayerRelocatorTest::generateFooting);

        assertThat(rescue).isEqualTo(new PlayerRelocator.LoginRescue.Stay());
    }

    @Test
    void chooseRescue_leavesPlayerInPlace_whenBuriedInGeneratedTerrain() {
        ResourceKey<Level> generated = EgmdDimensionFaker.pickRegistered(
                        dimension -> dimension.terrain() instanceof Terrain.Generated)
                .levelKey();
        BlockPos clearSpace = new BlockPosFaker().generate();

        PlayerRelocator.LoginRescue rescue =
                PlayerRelocator.chooseRescue(generated, () -> new PlayerRelocator.Footing.Buried(clearSpace));

        assertThat(rescue).isEqualTo(new PlayerRelocator.LoginRescue.Stay());
    }

    @Test
    void chooseRescue_leavesPlayerInPlace_whenPlayerIsStanding() {
        PlayerRelocator.LoginRescue rescue = PlayerRelocator.chooseRescue(flat, PlayerRelocator.Footing.Standing::new);

        assertThat(rescue).isEqualTo(new PlayerRelocator.LoginRescue.Stay());
    }

    @Test
    void chooseRescue_liftsPlayerToClearSpace_whenPlayerIsBuriedBelowClearSpace() {
        BlockPos clearSpace = new BlockPosFaker().generate();

        PlayerRelocator.LoginRescue rescue =
                PlayerRelocator.chooseRescue(flat, () -> new PlayerRelocator.Footing.Buried(clearSpace));

        assertThat(rescue).isEqualTo(new PlayerRelocator.LoginRescue.LiftToClearSpace(clearSpace));
    }

    @Test
    void chooseRescue_sendsPlayerToOverworld_whenPlayerIsEntombed() {
        PlayerRelocator.LoginRescue rescue = PlayerRelocator.chooseRescue(flat, PlayerRelocator.Footing.Entombed::new);

        assertThat(rescue).isEqualTo(new PlayerRelocator.LoginRescue.SendToOverworld());
    }

    private static PlayerRelocator.Footing generateFooting() {
        return FAKER.options()
                .option(
                        new PlayerRelocator.Footing.Standing(),
                        new PlayerRelocator.Footing.Buried(new BlockPosFaker().generate()),
                        new PlayerRelocator.Footing.Entombed());
    }
}
