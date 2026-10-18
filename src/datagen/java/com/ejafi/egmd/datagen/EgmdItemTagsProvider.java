package com.ejafi.egmd.datagen;

import com.ejafi.egmd.Egmd;
import com.ejafi.egmd.portal.PortalIgnitionHandler;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

final class EgmdItemTagsProvider extends ItemTagsProvider {
    private static final ResourceLocation UNBREAKABLE_NETHERITE_FLINT_AND_STEEL = ResourceLocation.fromNamespaceAndPath(
            "create_unbreakable_tools_series", "unbreakable_netherite_flint_and_steel");

    EgmdItemTagsProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookup,
            CompletableFuture<TagsProvider.TagLookup<Block>> blockTags,
            ExistingFileHelper fileHelper) {
        super(output, lookup, blockTags, Egmd.MOD_ID, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(PortalIgnitionHandler.PORTAL_IGNITERS)
                .add(Items.FLINT_AND_STEEL)
                .addOptionalTag(Tags.Items.TOOLS_IGNITER)
                .addOptional(UNBREAKABLE_NETHERITE_FLINT_AND_STEEL);
    }
}
