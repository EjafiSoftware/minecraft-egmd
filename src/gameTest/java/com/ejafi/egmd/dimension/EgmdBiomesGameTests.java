package com.ejafi.egmd.dimension;

import com.ejafi.egmd.Egmd;
import com.ejafi.egmd.GameTestTemplates;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Egmd.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EgmdBiomesGameTests {
    private EgmdBiomesGameTests() {}

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void haunted_woods_allows_full_moon_slime_spawns_by_default(GameTestHelper helper) {
        boolean allowsSlimes = getHauntedWoods(helper).is(SlimeSpawnChecker.ALLOWS_FULL_MOON_SLIME_SPAWNS);

        helper.assertValueEqual(allowsSlimes, true, "full moon slime spawns allowed");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void haunted_woods_excludes_moon_phase_slime_spawns_by_default(GameTestHelper helper) {
        boolean allowsSlimes = getHauntedWoods(helper).is(BiomeTags.ALLOWS_SURFACE_SLIME_SPAWNS);

        helper.assertValueEqual(allowsSlimes, false, "vanilla surface slime spawns allowed");
        helper.succeed();
    }

    private static Holder<Biome> getHauntedWoods(GameTestHelper helper) {
        return helper.getLevel()
                .registryAccess()
                .registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(EgmdBiomes.HAUNTED_WOODS);
    }
}
