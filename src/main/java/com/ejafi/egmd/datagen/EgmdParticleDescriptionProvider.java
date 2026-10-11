package com.ejafi.egmd.datagen;

import com.ejafi.egmd.portal.PortalRegisters;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.ParticleDescriptionProvider;

final class EgmdParticleDescriptionProvider extends ParticleDescriptionProvider {
    private static final ResourceLocation GENERIC_SPRITES = ResourceLocation.withDefaultNamespace("generic");
    private static final int GENERIC_SPRITE_COUNT = 8;

    EgmdParticleDescriptionProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, fileHelper);
    }

    @Override
    protected void addDescriptions() {
        spriteSet(PortalRegisters.PORTAL_SPARK.get(), GENERIC_SPRITES, GENERIC_SPRITE_COUNT, false);
    }
}
