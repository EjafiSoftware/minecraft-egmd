package com.ejafi.egmd.dimension;

import static org.assertj.core.api.Assertions.assertThat;

import com.ejafi.egmd.BlockPosFaker;
import net.datafaker.Faker;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PlayerRelocatorTest {
    private static final Faker FAKER = new Faker();

    private final ResourceKey<Level> mining =
            MiningDimensionFaker.pickRegistered().levelKey();

    @AfterEach
    void clearPendingRegenerations() {
        PendingRegenerations.clear();
    }

    @Test
    void chooseRescue_leavesPlayerInPlace_whenDimensionIsNotMining() {
        ResourceKey<Level> vanilla = FAKER.options().option(Level.OVERWORLD, Level.NETHER, Level.END);

        PlayerRelocator.LoginRescue rescue =
                PlayerRelocator.chooseRescue(vanilla, PlayerRelocatorTest::generateFooting);

        assertThat(rescue).isEqualTo(new PlayerRelocator.LoginRescue.Stay());
    }

    @Test
    void chooseRescue_movesPlayerOutOfClosedDimension_whenDimensionAwaitsRegeneration() {
        PendingRegenerations.mark(mining);

        PlayerRelocator.LoginRescue rescue = PlayerRelocator.chooseRescue(mining, PlayerRelocatorTest::generateFooting);

        assertThat(rescue).isEqualTo(new PlayerRelocator.LoginRescue.LeaveClosedDimension());
    }

    @Test
    void chooseRescue_leavesPlayerInPlace_whenPlayerIsStanding() {
        PlayerRelocator.LoginRescue rescue =
                PlayerRelocator.chooseRescue(mining, PlayerRelocator.Footing.Standing::new);

        assertThat(rescue).isEqualTo(new PlayerRelocator.LoginRescue.Stay());
    }

    @Test
    void chooseRescue_liftsPlayerToClearSpace_whenPlayerIsBuriedBelowClearSpace() {
        BlockPos clearSpace = new BlockPosFaker().generate();

        PlayerRelocator.LoginRescue rescue =
                PlayerRelocator.chooseRescue(mining, () -> new PlayerRelocator.Footing.Buried(clearSpace));

        assertThat(rescue).isEqualTo(new PlayerRelocator.LoginRescue.LiftToClearSpace(clearSpace));
    }

    @Test
    void chooseRescue_sendsPlayerToOverworld_whenPlayerIsEntombed() {
        PlayerRelocator.LoginRescue rescue =
                PlayerRelocator.chooseRescue(mining, PlayerRelocator.Footing.Entombed::new);

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
