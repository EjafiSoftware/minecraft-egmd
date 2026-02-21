package com.ejafi.egmd.core.registration;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;

import java.util.List;

public final class PortalVariants {
    public static final PortalVariant OVERWORLD = new PortalVariant(
            "mining_portal", ModDimensions.OVERWORLD_MINING_LEVEL_KEY,
            Blocks.COBBLESTONE, MapColor.COLOR_PURPLE,
            ModDimensions.OVERWORLD_MINING_SURFACE_Y, 0x00FF00);

    public static final PortalVariant NETHER = new PortalVariant(
            "nether_mining_portal", ModDimensions.NETHER_MINING_LEVEL_KEY,
            Blocks.NETHERRACK, MapColor.COLOR_RED,
            ModDimensions.NETHER_MINING_SURFACE_Y, 0xFF0000);

    public static final PortalVariant END = new PortalVariant(
            "end_mining_portal", ModDimensions.END_MINING_LEVEL_KEY,
            Blocks.END_STONE, MapColor.COLOR_YELLOW,
            ModDimensions.END_MINING_SURFACE_Y, 0xFFFF00);

    public static final List<PortalVariant> ALL = List.of(OVERWORLD, NETHER, END);

    private PortalVariants() {}
}
