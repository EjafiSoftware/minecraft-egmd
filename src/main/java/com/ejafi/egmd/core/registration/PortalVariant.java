package com.ejafi.egmd.core.registration;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

public record PortalVariant(
        String id,
        ResourceKey<Level> dimensionKey,
        Block frameBlock,
        MapColor mapColor,
        int surfaceY,
        int tintColor
) {}
