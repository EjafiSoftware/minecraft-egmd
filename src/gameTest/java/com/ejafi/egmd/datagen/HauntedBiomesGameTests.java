package com.ejafi.egmd.datagen;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.GameTestTemplates;
import com.ejafi.egmd.dimension.SlimeSpawnChecker;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(EGMD.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HauntedBiomesGameTests {
    private HauntedBiomesGameTests() {}

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
                .getHolderOrThrow(HauntedBiomes.HAUNTED_WOODS);
    }
}
