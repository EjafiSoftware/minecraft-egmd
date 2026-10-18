package com.ejafi.egmd.datagen;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.dimension.DimensionMessages;
import com.ejafi.egmd.dimension.EgmdBiomes;
import com.ejafi.egmd.pearl.PearlBlocks;
import com.ejafi.egmd.portal.PortalConfig;
import com.ejafi.egmd.portal.PortalMessages;
import com.ejafi.egmd.portal.PortalRegisters;
import com.ejafi.egmd.portal.PortalVariant;
import com.ejafi.egmd.portal.PortalVariants;
import net.minecraft.Util;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.data.LanguageProvider;

final class EgmdLanguageProvider extends LanguageProvider {
    EgmdLanguageProvider(PackOutput output) {
        super(output, EGMD.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addPortal(PortalVariants.OVERWORLD_MINING, "Mining Portal");
        addPortal(PortalVariants.NETHER_MINING, "Nether Mining Portal");
        addPortal(PortalVariants.END_MINING, "End Mining Portal");
        addPortal(PortalVariants.HAUNTED, "Haunted Portal");
        addBlock(PearlBlocks.PEARL_BLOCK, "Pearl Block");

        addBiomeName(EgmdBiomes.MINING_PLAINS, "Mining Plains");
        addBiomeName(EgmdBiomes.NETHER_MINING_WASTES, "Nether Mining Wastes");
        addBiomeName(EgmdBiomes.END_MINING_WASTES, "End Mining Wastes");
        addBiomeName(EgmdBiomes.HAUNTED_WOODS, "Haunted Woods");

        add(
                DimensionMessages.LOGIN_RELOCATED,
                "This dimension changed while you were away, so you have been moved somewhere safe.");
        add(DimensionMessages.BED_REFUSED, "You can't sleep in this dimension.");

        add(PortalMessages.NO_SAFE_LOCATION, "The portal could not find a safe place to open on the other side.");
        addCornerHint(PortalVariants.OVERWORLD_MINING, "Mining Portal corners must be dirt, gravel, or sand.");
        addCornerHint(PortalVariants.NETHER_MINING, "Nether Mining Portal corners must be cobblestone or stone.");
        addCornerHint(
                PortalVariants.END_MINING, "End Mining Portal corners must be netherrack, obsidian, or prismarine.");
        addCornerHint(PortalVariants.HAUNTED, "Haunted Portal corners must be carved pumpkins or jack o'lanterns.");

        add(PortalConfig.PORTAL_SEARCH_RADIUS_TRANSLATION, "Portal Search Radius");
        add(PortalConfig.PORTAL_SEARCH_RADIUS_TRANSLATION + ".tooltip", PortalConfig.PORTAL_SEARCH_RADIUS_DESCRIPTION);
    }

    private void addPortal(PortalVariant variant, String name) {
        addBlock(PortalRegisters.getPortalBlock(variant), name);
    }

    private void addCornerHint(PortalVariant variant, String hint) {
        add(variant.getCornerHintKey(), hint);
    }

    private void addBiomeName(ResourceKey<Biome> biome, String name) {
        add(Util.makeDescriptionId("biome", biome.location()), name);
    }
}
