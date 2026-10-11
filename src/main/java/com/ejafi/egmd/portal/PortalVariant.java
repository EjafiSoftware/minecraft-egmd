package com.ejafi.egmd.portal;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.dimension.MiningDimension;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

public record PortalVariant(
        String id,
        MiningDimension dimension,
        Block frameBlock,
        Block defaultCornerBlock,
        String cornerHintKey,
        MapColor mapColor,
        int tintColor) {
    public TagKey<Block> getCornerTag() {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(EGMD.MOD_ID, id + "_corners"));
    }
}
