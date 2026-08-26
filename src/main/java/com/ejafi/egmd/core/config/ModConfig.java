package com.ejafi.egmd.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue PORTAL_SEARCH_RADIUS = BUILDER
            .comment("Block radius to search for an existing portal when teleporting (default: 128). Scaled down in dimensions with compressed coordinates.")
            .defineInRange("portalSearchRadius", 128, 16, 512);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ModConfig() {}
}
