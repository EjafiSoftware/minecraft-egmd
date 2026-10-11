package com.ejafi.egmd.portal;

import com.ejafi.egmd.EGMD;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class PortalConfig {
    public static final String PORTAL_SEARCH_RADIUS_TRANSLATION = EGMD.MOD_ID + ".configuration.portalSearchRadius";
    public static final String PORTAL_SEARCH_RADIUS_DESCRIPTION =
            "Block radius to search for an existing portal when teleporting. Scaled down in dimensions with compressed coordinates. Large values make the first search in an unexplored area load a lot of point-of-interest data, which can cause a lag spike.";

    static final int MIN_PORTAL_SEARCH_RADIUS = 16;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static final ModConfigSpec.IntValue PORTAL_SEARCH_RADIUS = BUILDER.comment(PORTAL_SEARCH_RADIUS_DESCRIPTION)
            .translation(PORTAL_SEARCH_RADIUS_TRANSLATION)
            .defineInRange("portalSearchRadius", 128, MIN_PORTAL_SEARCH_RADIUS, 512);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private PortalConfig() {}
}
