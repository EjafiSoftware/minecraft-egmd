package com.ejafi.egmd.portal;

import com.ejafi.egmd.dimension.MiningDimension;
import java.util.List;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;

public final class PortalVariants {
    public static final PortalVariant OVERWORLD = new PortalVariant(
            "mining_portal",
            MiningDimension.OVERWORLD,
            Blocks.COBBLESTONE,
            Blocks.DIRT,
            PortalMessages.MINING_PORTAL_CORNERS,
            MapColor.COLOR_PURPLE,
            0x00FF00);

    public static final PortalVariant NETHER = new PortalVariant(
            "nether_mining_portal",
            MiningDimension.NETHER,
            Blocks.NETHERRACK,
            Blocks.COBBLESTONE,
            PortalMessages.NETHER_MINING_PORTAL_CORNERS,
            MapColor.COLOR_RED,
            0xFF0000);

    public static final PortalVariant END = new PortalVariant(
            "end_mining_portal",
            MiningDimension.END,
            Blocks.END_STONE,
            Blocks.NETHERRACK,
            PortalMessages.END_MINING_PORTAL_CORNERS,
            MapColor.COLOR_YELLOW,
            0xFFFF00);

    static final List<PortalVariant> ALL = List.of(OVERWORLD, NETHER, END);

    private PortalVariants() {}
}
