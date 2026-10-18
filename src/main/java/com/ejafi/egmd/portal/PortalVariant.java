package com.ejafi.egmd.portal;

import com.ejafi.egmd.Egmd;
import com.ejafi.egmd.dimension.EgmdDimension;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

public record PortalVariant(
        String id,
        EgmdDimension dimension,
        Block frameBlock,
        Block defaultCornerBlock,
        MapColor mapColor,
        int tintColor) {
    public TagKey<Block> getCornerTag() {
        return TagKey.create(Registries.BLOCK, Egmd.createLocation(id + "_corners"));
    }

    public String getCornerHintKey() {
        return "message." + Egmd.MOD_ID + ".portal." + id + "_corners";
    }
}
