package com.ejafi.egmd.portal;

import static com.ejafi.egmd.portal.PortalGameTestSupport.FLOOR_CENTER;

import com.ejafi.egmd.Egmd;
import com.ejafi.egmd.GameTestTemplates;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Egmd.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PortalPositionFinderGameTests {
    private static final int CANOPY_HEIGHT = PortalFrame.MIN_HEIGHT + 1;

    private PortalPositionFinderGameTests() {}

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7, skyAccess = true)
    public static void search_spiral_on_surface_returns_south_east_neighbor_when_container_sits_on_target_column(
            GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos center = helper.absolutePos(FLOOR_CENTER);
        BlockPos chest = center.atY(level.getHeight(Heightmap.Types.MOTION_BLOCKING, center.getX(), center.getZ()));
        level.setBlockAndUpdate(chest, Blocks.CHEST.defaultBlockState());

        Optional<PortalFootprint> result =
                PortalPositionFinder.searchSpiralOnSurface(level, new PortalFootprint(center, Axis.X));

        PortalFootprint expected = new PortalFootprint(chest.east().south(), Axis.X);
        helper.assertValueEqual(result, Optional.of(expected), "site");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7, skyAccess = true)
    public static void search_spiral_on_surface_returns_ground_under_leaves_when_leaves_cover_target_column(
            GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos center = helper.absolutePos(FLOOR_CENTER);
        BlockPos ground = center.atY(level.getHeight(Heightmap.Types.MOTION_BLOCKING, center.getX(), center.getZ()));
        level.setBlockAndUpdate(ground.above(CANOPY_HEIGHT), Blocks.OAK_LEAVES.defaultBlockState());

        Optional<PortalFootprint> result =
                PortalPositionFinder.searchSpiralOnSurface(level, new PortalFootprint(center, Axis.X));

        helper.assertValueEqual(result, Optional.of(new PortalFootprint(ground, Axis.X)), "site");
        helper.succeed();
    }
}
