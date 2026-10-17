package com.ejafi.egmd.datagen;

import com.ejafi.egmd.EGMD;
import java.util.List;
import java.util.Set;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = EGMD.MOD_ID)
public final class DataGatherer {
    private DataGatherer() {}

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        ExistingFileHelper fileHelper = event.getExistingFileHelper();

        event.createDatapackRegistryObjects(new RegistrySetBuilder()
                .add(Registries.DIMENSION_TYPE, EgmdLevels::bootstrapDimensionTypes)
                .add(Registries.CONFIGURED_FEATURE, context -> {
                    MiningOres.bootstrapConfiguredFeatures(context);
                    HauntedFeatures.bootstrapConfiguredFeatures(context);
                })
                .add(Registries.PLACED_FEATURE, context -> {
                    MiningOres.bootstrapPlacedFeatures(context);
                    HauntedFeatures.bootstrapPlacedFeatures(context);
                })
                .add(Registries.BIOME, context -> {
                    MiningBiomes.bootstrap(context);
                    HauntedBiomes.bootstrap(context);
                })
                .add(Registries.LEVEL_STEM, EgmdLevels::bootstrapLevelStems));

        event.createProvider(output -> new EgmdBlockStateProvider(output, fileHelper));
        event.createProvider(output -> new EgmdParticleDescriptionProvider(output, fileHelper));
        event.createProvider(EgmdLanguageProvider::new);
        event.createProvider((output, lookup) -> new LootTableProvider(
                output,
                Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(EgmdBlockLootProvider::new, LootContextParamSets.BLOCK)),
                lookup));
        event.createProvider((output, lookup) -> new EgmdBiomeTagsProvider(output, lookup, fileHelper));
        event.createBlockAndItemTags(
                (output, lookup) -> new EgmdBlockTagsProvider(output, lookup, fileHelper),
                (output, lookup, blockTags) -> new EgmdItemTagsProvider(output, lookup, blockTags, fileHelper));
    }
}
