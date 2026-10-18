package com.ejafi.egmd.portal;

import com.ejafi.egmd.BlockPosFaker;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/** Stands in for a level in unit tests, because the FML test runtime loads no world. */
final class FakeLevel implements CollisionGetter, LevelWriter {
    private final Map<BlockPos, BlockState> blocks = new HashMap<>();
    private final WorldBorder worldBorder = new WorldBorder();

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return blocks.getOrDefault(pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public int getHeight() {
        return BlockPosFaker.MAX_BUILD_Y - BlockPosFaker.MIN_BUILD_Y;
    }

    @Override
    public int getMinBuildHeight() {
        return BlockPosFaker.MIN_BUILD_Y;
    }

    @Override
    public WorldBorder getWorldBorder() {
        return worldBorder;
    }

    @Override
    public BlockGetter getChunkForCollisions(int chunkX, int chunkZ) {
        return this;
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB collisionBox) {
        return List.of();
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft) {
        blocks.put(pos.immutable(), state);
        return true;
    }

    @Override
    public boolean removeBlock(BlockPos pos, boolean isMoving) {
        blocks.remove(pos);
        return true;
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity, int recursionDepth) {
        blocks.remove(pos);
        return true;
    }
}
