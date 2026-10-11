package com.ejafi.egmd.portal;

import com.ejafi.egmd.dimension.MiningDimension;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

public record PortalVariant(String id, MiningDimension dimension, Block frameBlock, MapColor mapColor, int tintColor) {}
