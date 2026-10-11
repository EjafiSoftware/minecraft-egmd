package com.ejafi.egmd.portal;

import static com.ejafi.egmd.portal.PortalCornersFaker.fillCorners;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.datafaker.Faker;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

class PortalCornerMaterialsTest {
    private static final Faker FAKER = new Faker();

    private final Block fallback = FAKER.options().option(Blocks.COBBLESTONE, Blocks.NETHERRACK, Blocks.END_STONE);
    private final Map<PortalCorner, BlockState> source = new PortalCornersFaker().generate();
    private final PortalCorner rejectedCorner = FAKER.options().option(PortalCorner.class);

    @Test
    void copyAll_keepsEachSourceStateAtItsCorner_whenEveryStateIsAccepted() {
        var materials = new PortalCornerMaterials(state -> true, fallback);

        Map<PortalCorner, BlockState> copied = materials.copyAll(source);

        assertThat(copied).isEqualTo(source);
    }

    @Test
    void copyAll_usesFallback_whenSourceStateIsRejected() {
        BlockState rejected = Objects.requireNonNull(source.get(rejectedCorner));
        var materials = new PortalCornerMaterials(state -> !state.equals(rejected), fallback);

        Map<PortalCorner, BlockState> copied = materials.copyAll(source);

        Map<PortalCorner, BlockState> expected = new HashMap<>(source);
        expected.put(rejectedCorner, fallback.defaultBlockState());
        assertThat(copied).isEqualTo(expected);
    }

    @Test
    void copyAll_copiesHostBlock_whenSourceStateIsInfestedAndHostIsAccepted() {
        var materials = new PortalCornerMaterials(state -> state.is(Blocks.STONE), fallback);

        Map<PortalCorner, BlockState> copied =
                materials.copyAll(fillCorners(Blocks.INFESTED_STONE.defaultBlockState()));

        assertThat(copied).isEqualTo(fillCorners(Blocks.STONE.defaultBlockState()));
    }

    @Test
    void copyAll_copiesDefaultStateOfBlock_whenSourceStateHasChangedProperties() {
        var materials = new PortalCornerMaterials(state -> true, fallback);
        BlockState snowyGrass = Blocks.GRASS_BLOCK.defaultBlockState().setValue(SnowyDirtBlock.SNOWY, true);

        Map<PortalCorner, BlockState> copied = materials.copyAll(fillCorners(snowyGrass));

        assertThat(copied).isEqualTo(fillCorners(Blocks.GRASS_BLOCK.defaultBlockState()));
    }

    @Test
    void copyAll_usesFallbackAtBottomCorners_whenSourceStateIsFallingBlock() {
        var materials = new PortalCornerMaterials(state -> true, fallback);
        BlockState sand = Blocks.SAND.defaultBlockState();

        Map<PortalCorner, BlockState> copied = materials.copyAll(fillCorners(sand));

        assertThat(copied)
                .isEqualTo(Map.of(
                        PortalCorner.LOW_BOTTOM,
                        fallback.defaultBlockState(),
                        PortalCorner.HIGH_BOTTOM,
                        fallback.defaultBlockState(),
                        PortalCorner.LOW_TOP,
                        sand,
                        PortalCorner.HIGH_TOP,
                        sand));
    }

    @Test
    void copyAll_usesFallbackAtEveryCorner_whenSourceHasNoCorners() {
        var materials = new PortalCornerMaterials(state -> true, fallback);

        Map<PortalCorner, BlockState> copied = materials.copyAll(Map.of());

        assertThat(copied).isEqualTo(fillCorners(fallback.defaultBlockState()));
    }

    @Test
    void acceptsAll_acceptsCorners_whenEveryStateIsAccepted() {
        var materials = new PortalCornerMaterials(state -> true, fallback);

        boolean accepted = materials.acceptsAll(source);

        assertThat(accepted).isTrue();
    }

    @Test
    void acceptsAll_rejectsCorners_whenOneStateIsRejected() {
        BlockState rejected = Objects.requireNonNull(source.get(rejectedCorner));
        var materials = new PortalCornerMaterials(state -> !state.equals(rejected), fallback);

        boolean accepted = materials.acceptsAll(source);

        assertThat(accepted).isFalse();
    }

    @Test
    void acceptsAll_acceptsCorners_whenInfestedHostIsAccepted() {
        var materials = new PortalCornerMaterials(state -> state.is(Blocks.STONE), fallback);

        boolean accepted = materials.acceptsAll(fillCorners(Blocks.INFESTED_STONE.defaultBlockState()));

        assertThat(accepted).isTrue();
    }

    @Test
    void acceptsAll_rejectsCorners_whenFrameHasNoCorners() {
        var materials = new PortalCornerMaterials(state -> true, fallback);

        boolean accepted = materials.acceptsAll(Map.of());

        assertThat(accepted).isFalse();
    }
}
