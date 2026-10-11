package com.ejafi.egmd.datagen;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.pearl.PearlBlocks;
import com.ejafi.egmd.portal.MiningPortalBlock;
import com.ejafi.egmd.portal.PortalRegisters;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

final class EgmdBlockStateProvider extends BlockStateProvider {
    private static final ResourceLocation PORTAL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(EGMD.MOD_ID, "block/mining_portal");
    private static final String PORTAL_TEXTURE_KEY = "portal";
    private static final String TRANSLUCENT = "minecraft:translucent";
    private static final float PIXELS_PER_BLOCK = 16.0F;
    private static final PortalPlane NORTH_SOUTH = new PortalPlane("ns", Direction.NORTH, Axis.X);
    private static final PortalPlane EAST_WEST = new PortalPlane("ew", Direction.EAST, Axis.Z);

    EgmdBlockStateProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, EGMD.MOD_ID, fileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile northSouth = createPortalModel(NORTH_SOUTH);
        ModelFile eastWest = createPortalModel(EAST_WEST);
        for (DeferredBlock<MiningPortalBlock> portal : PortalRegisters.PORTAL_BLOCKS.values()) {
            getVariantBuilder(portal.get())
                    .partialState()
                    .with(MiningPortalBlock.AXIS, Axis.X)
                    .modelForState()
                    .modelFile(northSouth)
                    .addModel()
                    .partialState()
                    .with(MiningPortalBlock.AXIS, Axis.Z)
                    .modelForState()
                    .modelFile(eastWest)
                    .addModel();
        }

        simpleBlockWithItem(
                PearlBlocks.PEARL_BLOCK.get(),
                models().cubeAll(PearlBlocks.PEARL_BLOCK.getId().getPath(), mcLoc("block/sculk_shrieker_inner_top")));
    }

    private ModelFile createPortalModel(PortalPlane plane) {
        BlockModelBuilder model = models().withExistingParent(
                        "mining_portal_" + plane.suffix(), mcLoc("block/nether_portal_" + plane.suffix()))
                .renderType(TRANSLUCENT)
                .texture("particle", PORTAL_TEXTURE)
                .texture(PORTAL_TEXTURE_KEY, PORTAL_TEXTURE);
        AABB pane = MiningPortalBlock.getPaneShape(plane.axis()).bounds();
        ModelBuilder<BlockModelBuilder>.ElementBuilder element = model.element()
                .from(toPixels(pane.minX), toPixels(pane.minY), toPixels(pane.minZ))
                .to(toPixels(pane.maxX), toPixels(pane.maxY), toPixels(pane.maxZ));
        for (Direction face : List.of(plane.front(), plane.front().getOpposite())) {
            element.face(face).texture("#" + PORTAL_TEXTURE_KEY).tintindex(0);
        }
        return model;
    }

    private static float toPixels(double blockCoordinate) {
        return (float) blockCoordinate * PIXELS_PER_BLOCK;
    }

    private record PortalPlane(String suffix, Direction front, Axis axis) {}
}
