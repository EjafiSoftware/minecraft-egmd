package com.ejafi.egmd.portal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.datafaker.Faker;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

final class PortalCornersFaker {
    private static final Faker FAKER = new Faker();
    private static final List<Block> NON_FRAME_BLOCKS = List.of(
            Blocks.DIRT,
            Blocks.STONE,
            Blocks.GRANITE,
            Blocks.DIORITE,
            Blocks.ANDESITE,
            Blocks.OBSIDIAN,
            Blocks.PRISMARINE,
            Blocks.DARK_PRISMARINE,
            Blocks.MOSSY_COBBLESTONE,
            Blocks.DEEPSLATE);

    Map<PortalCorner, BlockState> generate() {
        List<Block> shuffled = new ArrayList<>(NON_FRAME_BLOCKS);
        Collections.shuffle(shuffled, new Random(FAKER.random().nextLong()));
        PortalCorner[] corners = PortalCorner.values();
        return IntStream.range(0, corners.length)
                .boxed()
                .collect(Collectors.toUnmodifiableMap(
                        index -> corners[index], index -> shuffled.get(index).defaultBlockState()));
    }

    static Map<PortalCorner, BlockState> fillCorners(BlockState state) {
        return PortalCorner.mapAll(corner -> state);
    }
}
