package com.ejafi.egmd.datagen;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.pearl.PearlBlocks;
import com.ejafi.egmd.portal.PortalVariants;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

final class EgmdBlockTagsProvider extends BlockTagsProvider {
    EgmdBlockTagsProvider(
            PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper fileHelper) {
        super(output, lookup, EGMD.MOD_ID, fileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(PearlBlocks.PEARL_BLOCK.get());

        tag(PortalVariants.OVERWORLD_MINING.getCornerTag())
                .add(
                        Blocks.DIRT,
                        Blocks.COARSE_DIRT,
                        Blocks.ROOTED_DIRT,
                        Blocks.GRASS_BLOCK,
                        Blocks.PODZOL,
                        Blocks.MYCELIUM)
                .addTag(Tags.Blocks.GRAVELS)
                .addTag(Tags.Blocks.SANDS);

        tag(PortalVariants.NETHER_MINING.getCornerTag())
                .addTag(Tags.Blocks.COBBLESTONES)
                .addTag(Tags.Blocks.STONES);

        tag(PortalVariants.END_MINING.getCornerTag())
                .addTag(Tags.Blocks.NETHERRACKS)
                .addTag(Tags.Blocks.OBSIDIANS)
                .add(Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.DARK_PRISMARINE);

        tag(PortalVariants.HAUNTED.getCornerTag()).add(Blocks.CARVED_PUMPKIN, Blocks.JACK_O_LANTERN);
    }
}
