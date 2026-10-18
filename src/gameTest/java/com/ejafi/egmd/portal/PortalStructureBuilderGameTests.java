package com.ejafi.egmd.portal;

import com.ejafi.egmd.Egmd;
import com.ejafi.egmd.GameTestTemplates;
import net.minecraft.core.Direction.Axis;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Egmd.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PortalStructureBuilderGameTests {
    private PortalStructureBuilderGameTests() {}

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void build_places_portal_blocks_east_of_origin_when_axis_is_x(GameTestHelper helper) {
        PortalGameTestSupport.buildOpenPortalAtOrigin(helper, Axis.X);

        PortalGameTestSupport.assertPortalFillsInterior(helper, Axis.X);
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void build_places_portal_blocks_south_of_origin_when_axis_is_z(GameTestHelper helper) {
        PortalGameTestSupport.buildOpenPortalAtOrigin(helper, Axis.Z);

        PortalGameTestSupport.assertPortalFillsInterior(helper, Axis.Z);
        helper.succeed();
    }
}
