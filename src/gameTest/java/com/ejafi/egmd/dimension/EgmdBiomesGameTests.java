package com.ejafi.egmd.dimension;

import com.ejafi.egmd.Egmd;
import com.ejafi.egmd.GameTestTemplates;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Egmd.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EgmdBiomesGameTests {
    private EgmdBiomesGameTests() {}

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void haunted_woods_holds_only_full_moon_slime_spawn_tag_by_default(GameTestHelper helper) {
        Set<TagKey<Biome>> tags = helper.getLevel()
                .registryAccess()
                .registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(EgmdBiomes.HAUNTED_WOODS)
                .tags()
                .collect(Collectors.toUnmodifiableSet());

        helper.assertValueEqual(tags, Set.of(SlimeSpawnChecker.ALLOWS_FULL_MOON_SLIME_SPAWNS), "biome tags");
        helper.succeed();
    }
}
