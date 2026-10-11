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

        add(DimensionMessages.NOT_MINING_DIMENSION, "Only EGMD dimensions can be regenerated with this command.");
        add(
                DimensionMessages.PLAYERS_STRANDED,
                "%s player(s) could not be moved out of %s. Its portals are closed, but players are still inside, so do not delete its folder yet.");
        add(
                DimensionMessages.EVACUATED,
                "Moved %s player(s) out of %s. Its portals stay closed until the server stops.");
        add(DimensionMessages.INSTRUCTIONS_TITLE, "To complete regeneration:");
        add(DimensionMessages.INSTRUCTIONS_STOP_SERVER, "1. Stop the server.");
        add(DimensionMessages.INSTRUCTIONS_DELETE_FOLDER, "2. Delete the folder: %s");
        add(DimensionMessages.INSTRUCTIONS_RESTART_SERVER, "3. Restart the server.");
        add(
                DimensionMessages.PLAYER_MOVED,
                "The dimension is being regenerated. You have been moved to the Overworld.");
        add(
                DimensionMessages.LOGIN_RELOCATED,
                "This mining dimension changed while you were away, so you have been moved somewhere safe.");
        add(
                DimensionMessages.LOGIN_CLOSED_FOR_REGENERATION,
                "This mining dimension is closed for regeneration, so you have been moved to the Overworld.");

        add(
                PortalMessages.CLOSED_FOR_REGENERATION,
                "This dimension is closed for regeneration until the server restarts.");
        add(PortalMessages.NO_SAFE_LOCATION, "The portal could not find a safe place to open on the other side.");

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
