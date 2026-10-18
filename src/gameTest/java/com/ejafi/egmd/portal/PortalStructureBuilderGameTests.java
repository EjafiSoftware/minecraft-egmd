package com.ejafi.egmd.portal;

import static com.ejafi.egmd.portal.PortalGameTestSupport.ORIGIN;

import com.ejafi.egmd.Egmd;
import com.ejafi.egmd.GameTestTemplates;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Egmd.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PortalStructureBuilderGameTests {
    private static final List<BlockPos> Z_INTERIOR = PortalGameTestSupport.listCellsBetween(
            ORIGIN, ORIGIN.offset(0, PortalFrame.MIN_HEIGHT - 1, PortalFrame.MIN_WIDTH - 1));

    private PortalStructureBuilderGameTests() {}

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void build_places_portal_blocks_east_of_origin_when_axis_is_x(GameTestHelper helper) {
        PortalGameTestSupport.buildOpenPortalAtOrigin(helper, Axis.X);

        PortalGameTestSupport.assertInteriorHolds(helper, PortalGameTestSupport.getPortalState(Axis.X));
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void build_places_portal_blocks_south_of_origin_when_axis_is_z(GameTestHelper helper) {
        PortalGameTestSupport.buildOpenPortalAtOrigin(helper, Axis.Z);

        helper.assertValueEqual(
                PortalGameTestSupport.readStates(helper, Z_INTERIOR),
                PortalGameTestSupport.listUniformStates(Z_INTERIOR, PortalGameTestSupport.getPortalState(Axis.Z)),
                "interior");
        helper.succeed();
    }
}
