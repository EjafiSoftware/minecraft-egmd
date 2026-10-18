package com.ejafi.egmd.datagen;

import com.ejafi.egmd.Egmd;
import com.ejafi.egmd.dimension.EgmdBiomes;
import com.ejafi.egmd.dimension.SlimeSpawnChecker;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

final class EgmdBiomeTagsProvider extends BiomeTagsProvider {
    EgmdBiomeTagsProvider(
            PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper fileHelper) {
        super(output, lookup, Egmd.MOD_ID, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(SlimeSpawnChecker.ALLOWS_FULL_MOON_SLIME_SPAWNS).add(EgmdBiomes.HAUNTED_WOODS);
    }
}
