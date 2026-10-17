package com.ejafi.egmd.portal;

import com.ejafi.egmd.dimension.EgmdDimensions;
import java.util.List;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;

public final class PortalVariants {
    public static final PortalVariant OVERWORLD_MINING = new PortalVariant(
            "mining_portal",
            EgmdDimensions.OVERWORLD_MINING,
            Blocks.COBBLESTONE,
            Blocks.DIRT,
            MapColor.COLOR_PURPLE,
            0x00FF00);

    public static final PortalVariant NETHER_MINING = new PortalVariant(
            "nether_mining_portal",
            EgmdDimensions.NETHER_MINING,
            Blocks.NETHERRACK,
            Blocks.COBBLESTONE,
            MapColor.COLOR_RED,
            0xFF0000);

    public static final PortalVariant END_MINING = new PortalVariant(
            "end_mining_portal",
            EgmdDimensions.END_MINING,
            Blocks.END_STONE,
            Blocks.NETHERRACK,
            MapColor.COLOR_YELLOW,
            0xFFFF00);

    public static final PortalVariant HAUNTED = new PortalVariant(
            "haunted_portal",
            EgmdDimensions.HAUNTED,
            Blocks.BONE_BLOCK,
            Blocks.JACK_O_LANTERN,
            MapColor.COLOR_ORANGE,
            0xFF8000);

    static final List<PortalVariant> ALL = List.of(OVERWORLD_MINING, NETHER_MINING, END_MINING, HAUNTED);

    private PortalVariants() {}
}
