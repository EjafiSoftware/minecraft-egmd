package com.ejafi.egmd.portal;

import static com.ejafi.egmd.portal.PortalGameTestSupport.FLOOR_CENTER;
import static com.ejafi.egmd.portal.PortalGameTestSupport.ORIGIN;

import com.ejafi.egmd.Egmd;
import com.ejafi.egmd.GameTestTemplates;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Egmd.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ExitPortalFinderGameTests {
    private static final BlockPos RAISED_PORTAL = new BlockPos(3, 5, 3);

    private ExitPortalFinderGameTests() {}

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void find_portal_around_returns_portal_when_it_sits_far_above_target(GameTestHelper helper) {
        helper.setBlock(RAISED_PORTAL, getPortalState());

        Optional<BlockPos> found = findPortalAroundFloorCenter(helper);

        helper.assertValueEqual(found, Optional.of(helper.absolutePos(RAISED_PORTAL)), "found portal");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void find_portal_around_returns_farther_portal_when_nearer_poi_holds_vanilla_nether_portal(
            GameTestHelper helper) {
        BlockPos stalePos = helper.absolutePos(ORIGIN);
        helper.setBlock(ORIGIN, getPortalState());
        helper.getLevel().getChunkAt(stalePos).setBlockState(stalePos, Blocks.NETHER_PORTAL.defaultBlockState(), false);
        helper.setBlock(RAISED_PORTAL, getPortalState());

        Optional<BlockPos> found = findPortalAroundFloorCenter(helper);

        helper.assertValueEqual(found, Optional.of(helper.absolutePos(RAISED_PORTAL)), "found portal");
        helper.succeed();
    }

    private static BlockState getPortalState() {
        return PortalRegisters.getPortalBlock(PortalVariants.OVERWORLD_MINING)
                .get()
                .defaultBlockState();
    }

    private static Optional<BlockPos> findPortalAroundFloorCenter(GameTestHelper helper) {
        return new ExitPortalFinder(helper.getLevel(), PortalVariants.OVERWORLD_MINING)
                .findPortalAround(helper.absolutePos(FLOOR_CENTER));
    }
}
