package com.ejafi.egmd.datagen;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.dimension.DimensionMessages;
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
        addPortal(PortalVariants.OVERWORLD, "Mining Portal");
        addPortal(PortalVariants.NETHER, "Nether Mining Portal");
        addPortal(PortalVariants.END, "End Mining Portal");
        addBlock(PearlBlocks.PEARL_BLOCK, "Pearl Block");

        addBiomeName(MiningBiomes.MINING_PLAINS, "Mining Plains");
        addBiomeName(MiningBiomes.NETHER_MINING_WASTES, "Nether Mining Wastes");
        addBiomeName(MiningBiomes.END_MINING_WASTES, "End Mining Wastes");

        add(
                DimensionMessages.LOGIN_RELOCATED,
                "This mining dimension changed while you were away, so you have been moved somewhere safe.");
        add(DimensionMessages.BED_REFUSED, "You can't sleep in a mining dimension.");

        add(PortalMessages.NO_SAFE_LOCATION, "The portal could not find a safe place to open on the other side.");
        add(PortalMessages.MINING_PORTAL_CORNERS, "Mining Portal corners must be dirt, gravel, or sand.");
        add(PortalMessages.NETHER_MINING_PORTAL_CORNERS, "Nether Mining Portal corners must be cobblestone or stone.");
        add(
                PortalMessages.END_MINING_PORTAL_CORNERS,
                "End Mining Portal corners must be netherrack, obsidian, or prismarine.");

        add(PortalConfig.PORTAL_SEARCH_RADIUS_TRANSLATION, "Portal Search Radius");
        add(PortalConfig.PORTAL_SEARCH_RADIUS_TRANSLATION + ".tooltip", PortalConfig.PORTAL_SEARCH_RADIUS_DESCRIPTION);
    }

    private void addPortal(PortalVariant variant, String name) {
        addBlock(PortalRegisters.getPortalBlock(variant), name);
    }

    private void addBiomeName(ResourceKey<Biome> biome, String name) {
        add(Util.makeDescriptionId("biome", biome.location()), name);
    }
}
