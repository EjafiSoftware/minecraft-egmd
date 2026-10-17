package com.ejafi.egmd.dimension;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.datafaker.Faker;
import net.minecraft.core.registries.BuiltInRegistries;
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

    @Test
    void allowsUse_refusesBed_whenDimensionForbidsBeds() {
        boolean allowed =
                BedGuard.allowsUse(generateBedState(), RestrictionsFaker.generateIncluding(Restriction.NO_BEDS));

        assertThat(allowed).isFalse();
    }

    @Test
    void allowsUse_acceptsBed_whenDimensionAllowsBeds() {
        boolean allowed =
                BedGuard.allowsUse(generateBedState(), RestrictionsFaker.generateExcluding(Restriction.NO_BEDS));

        assertThat(allowed).isTrue();
    }

    @Test
    void allowsUse_acceptsRespawnAnchor_whenDimensionForbidsBeds() {
        boolean allowed = BedGuard.allowsUse(
                Blocks.RESPAWN_ANCHOR.defaultBlockState(), RestrictionsFaker.generateIncluding(Restriction.NO_BEDS));

        assertThat(allowed).isTrue();
    }

    private static BlockState generateBedState() {
        return FAKER.options()
                .nextElement(BEDS)
                .defaultBlockState()
                .setValue(BedBlock.PART, FAKER.options().option(BedPart.class));
    }
}
