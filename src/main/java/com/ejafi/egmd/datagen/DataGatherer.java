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
                .add(Registries.DIMENSION_TYPE, MiningLevels::bootstrapDimensionTypes)
                .add(Registries.CONFIGURED_FEATURE, MiningOres::bootstrapConfiguredFeatures)
                .add(Registries.PLACED_FEATURE, MiningOres::bootstrapPlacedFeatures)
                .add(Registries.BIOME, MiningBiomes::bootstrap)
                .add(Registries.LEVEL_STEM, MiningLevels::bootstrapLevelStems));

        event.createProvider(output -> new EgmdBlockStateProvider(output, fileHelper));
        event.createProvider(output -> new EgmdParticleDescriptionProvider(output, fileHelper));
        event.createProvider(EgmdLanguageProvider::new);
        event.createProvider((output, lookup) -> new LootTableProvider(
                output,
                Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(EgmdBlockLootProvider::new, LootContextParamSets.BLOCK)),
                lookup));
        event.createBlockAndItemTags(
                (output, lookup) -> new EgmdBlockTagsProvider(output, lookup, fileHelper),
                (output, lookup, blockTags) -> new EgmdItemTagsProvider(output, lookup, blockTags, fileHelper));
    }
}
