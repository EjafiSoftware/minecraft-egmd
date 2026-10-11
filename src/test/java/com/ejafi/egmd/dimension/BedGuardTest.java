package com.ejafi.egmd.dimension;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.datafaker.Faker;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.junit.jupiter.api.Test;

class BedGuardTest {
    private static final Faker FAKER = new Faker();
    private static final List<Block> BEDS =
            BuiltInRegistries.BLOCK.stream().filter(BedBlock.class::isInstance).toList();

    private final ResourceKey<Level> mining =
            MiningDimensionFaker.pickRegistered().levelKey();

    @Test
    void allowsUse_refusesBed_whenDimensionIsMining() {
        boolean allowed = BedGuard.allowsUse(generateBedState(), mining);

        assertThat(allowed).isFalse();
    }

    @Test
    void allowsUse_acceptsBed_whenDimensionIsNotMining() {
        ResourceKey<Level> vanilla = FAKER.options().option(Level.OVERWORLD, Level.NETHER, Level.END);

        boolean allowed = BedGuard.allowsUse(generateBedState(), vanilla);

        assertThat(allowed).isTrue();
    }

    @Test
    void allowsUse_acceptsRespawnAnchor_whenDimensionIsMining() {
        boolean allowed = BedGuard.allowsUse(Blocks.RESPAWN_ANCHOR.defaultBlockState(), mining);

        assertThat(allowed).isTrue();
    }

    private static BlockState generateBedState() {
        return FAKER.options()
                .nextElement(BEDS)
                .defaultBlockState()
                .setValue(BedBlock.PART, FAKER.options().option(BedPart.class));
    }
}
