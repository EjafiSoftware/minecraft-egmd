package com.ejafi.egmd.portal;

import com.ejafi.egmd.Egmd;
import com.ejafi.egmd.GameTestTemplates;
import java.util.Collections;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Egmd.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PortalCornerMaterialsGameTests {
    private PortalCornerMaterialsGameTests() {}

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void create_rejects_corners_when_they_are_the_frame_block_of_any_variant(GameTestHelper helper) {
        List<Boolean> accepted = PortalVariants.ALL.stream()
                .map(variant -> PortalCornerMaterials.create(variant)
                        .acceptsAll(PortalCornersFaker.fillCorners(
                                variant.frameBlock().defaultBlockState())))
                .toList();

        helper.assertValueEqual(accepted, Collections.nCopies(PortalVariants.ALL.size(), false), "accepted");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void create_accepts_corners_when_they_are_the_default_corner_block_of_any_variant(
            GameTestHelper helper) {
        List<Boolean> accepted = PortalVariants.ALL.stream()
                .map(variant -> PortalCornerMaterials.create(variant)
                        .acceptsAll(PortalCornersFaker.fillCorners(
                                variant.defaultCornerBlock().defaultBlockState())))
                .toList();

        helper.assertValueEqual(accepted, Collections.nCopies(PortalVariants.ALL.size(), true), "accepted");
        helper.succeed();
    }
}
