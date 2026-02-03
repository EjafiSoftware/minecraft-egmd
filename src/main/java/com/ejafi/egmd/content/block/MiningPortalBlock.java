package com.ejafi.egmd.content.block;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.core.registration.ModDimensions;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Optional;

/**
 * Custom portal block for the Mining Dimension.
 * Reuses the nether portal texture but utilizes a configurable frame (Cobblestone or Netherrack).
 */
public class MiningPortalBlock extends Block implements Portal {
    public static final EnumProperty<Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    private final ResourceKey<Level> dimensionKey;
    private final Block frameBlock;

    public MiningPortalBlock(BlockBehaviour.Properties properties, ResourceKey<Level> dimensionKey, Block frameBlock) {
        super(properties);
        this.dimensionKey = dimensionKey;
        this.frameBlock = frameBlock;
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Axis.X));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Axis.Z ? Z_AXIS_AABB : X_AXIS_AABB;
    }

    public ResourceKey<Level> getDimensionKey() {
        return dimensionKey;
    }

    public Block getFrameBlock() {
        return frameBlock;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    public DimensionTransition getPortalDestination(ServerLevel serverLevel, Entity entity, BlockPos pos) {
        ResourceKey<Level> resourceKey = serverLevel.dimension() == this.dimensionKey ? Level.OVERWORLD : this.dimensionKey;
        ServerLevel destLevel = serverLevel.getServer().getLevel(resourceKey);
        if (destLevel == null) {
            return null;
        }

        WorldBorder worldBorder = destLevel.getWorldBorder();
        double scale = DimensionType.getTeleportationScale(serverLevel.dimensionType(), destLevel.dimensionType());

        // Use current position for scaling to ensure better alignment
        BlockPos targetPos = worldBorder.clampToBounds(entity.getX() * scale, entity.getY(), entity.getZ() * scale);

        return this.getExitPortal(destLevel, entity, pos, targetPos, worldBorder);
    }

    private DimensionTransition getExitPortal(ServerLevel destLevel, Entity entity, BlockPos pos, BlockPos targetPos, WorldBorder worldBorder) {
        Optional<BlockPos> optional = findPortalAround(destLevel, targetPos, worldBorder, this);
        BlockUtil.FoundRectangle rectangle;
        Axis axis;
        if (optional.isPresent()) {
            BlockPos portalPos = optional.get();
            BlockState state = destLevel.getBlockState(portalPos);
            axis = state.getValue(AXIS);
            rectangle = BlockUtil.getLargestRectangleAround(portalPos, axis, MiningPortalShape.MAX_WIDTH, Axis.Y, MiningPortalShape.MAX_HEIGHT, posToCheck -> destLevel.getBlockState(posToCheck) == state);
        } else {
            axis = entity.level().getBlockState(pos).getOptionalValue(AXIS).orElse(Axis.X);
            Optional<BlockUtil.FoundRectangle> optionalRectangle = this.createPortal(destLevel, targetPos, axis);
            if (optionalRectangle.isEmpty()) {
                EGMD.LOGGER.error("Unable to create a portal in dimension {}", destLevel.dimension().location());
                return null;
            }

            rectangle = optionalRectangle.get();
        }

        return getPortalTransition(destLevel, rectangle, axis);
    }

    private static DimensionTransition getPortalTransition(ServerLevel destLevel, BlockUtil.FoundRectangle rectangle, Axis axis) {
        BlockState exitState = destLevel.getBlockState(rectangle.minCorner);
        Axis exitAxis = exitState.getOptionalValue(AXIS).orElse(axis);

        // Position player relative to the portal frame
        Direction rightDir = exitAxis == Axis.X ? Direction.WEST : Direction.SOUTH;
        Direction frontDir = rightDir.getClockWise();

        // Center of the bottom of the portal opening
        Vec3 portalBottomCenter = Vec3.atLowerCornerOf(rectangle.minCorner).add(
                Vec3.atLowerCornerOf(rightDir.getNormal()).scale(rectangle.axis1Size / 2.0)
        );

        // Spawn 1.5 blocks in front of the portal to stay close for easy return-linking
        Vec3 spawnPos = portalBottomCenter.add(Vec3.atLowerCornerOf(frontDir.getNormal()).scale(1.5));

        // Face away from the portal
        float yRot = frontDir.toYRot();

        return new DimensionTransition(destLevel, spawnPos, Vec3.ZERO, yRot, 0.0F, DimensionTransition.DO_NOTHING);
    }

    private static Optional<BlockPos> findPortalAround(ServerLevel destLevel, BlockPos pos, WorldBorder worldBorder, Block block) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int searchRadius = 128;
        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int z = -searchRadius; z <= searchRadius; z++) {
                for (int y = -64; y <= 64; y++) {
                    mutablePos.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    if (worldBorder.isWithinBounds(mutablePos) && destLevel.getBlockState(mutablePos).is(block)) {
                        return Optional.of(mutablePos.immutable());
                    }
                }
            }
        }
        return Optional.empty();
    }

    private Optional<BlockUtil.FoundRectangle> createPortal(ServerLevel destLevel, BlockPos pos, Axis axis) {
        int maxY = destLevel.getHeight() - 1;
        BlockPos.MutableBlockPos mutablePos = pos.mutable();

        // If going to a mining dimension, we want it on the surface (roughly Y=68 in our flat worlds)
        if (destLevel.dimension() == ModDimensions.OVERWORLD_MINING_LEVEL_KEY || 
            destLevel.dimension() == ModDimensions.NETHER_MINING_LEVEL_KEY ||
            destLevel.dimension() == ModDimensions.END_MINING_LEVEL_KEY) {
            mutablePos.setY(68);
        }

        BlockPos bestPos = null;
        double bestDist = -1.0;
        WorldBorder worldBorder = destLevel.getWorldBorder();

        for (BlockPos.MutableBlockPos spiralPos : BlockPos.spiralAround(mutablePos, 16, Direction.EAST, Direction.SOUTH)) {
            int height = Math.min(maxY, destLevel.getHeight(Heightmap.Types.MOTION_BLOCKING, spiralPos.getX(), spiralPos.getZ()));
            if (worldBorder.isWithinBounds(spiralPos) && worldBorder.isWithinBounds(spiralPos.move(Direction.UP, 1))) {
                spiralPos.move(Direction.DOWN, 1);

                for (int y = height; y >= 0; --y) {
                    spiralPos.setY(y);
                    if (destLevel.isEmptyBlock(spiralPos)) {
                        int savedY = y;

                        while (y > 0 && destLevel.isEmptyBlock(spiralPos.move(Direction.DOWN))) {
                            --y;
                        }

                        if (y >= 0) {
                            spiralPos.setY(y);
                            if (canCreatePortalAt(destLevel, spiralPos, axis)) {
                                double dist = mutablePos.distSqr(spiralPos);
                                if (bestDist == -1.0 || dist < bestDist) {
                                    bestDist = dist;
                                    bestPos = spiralPos.immutable();
                                }
                            }
                        }

                        y = savedY;
                    }
                }
            }
        }

        if (bestPos == null) {
            // Fallback: try to find ANY valid spot if spiral search failed
            int height = Math.min(maxY, destLevel.getHeight(Heightmap.Types.MOTION_BLOCKING, mutablePos.getX(), mutablePos.getZ()));
            for (int y = height; y >= 0; --y) {
                mutablePos.setY(y);
                if (canCreatePortalAt(destLevel, mutablePos, axis)) {
                    bestPos = mutablePos.immutable();
                    break;
                }
            }
        }

        if (bestPos != null) {
            this.makePortalStructure(destLevel, bestPos, axis);
            return Optional.of(new BlockUtil.FoundRectangle(bestPos.immutable(), MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT));
        } else {
            return Optional.empty();
        }
    }

    private static boolean canCreatePortalAt(ServerLevel level, BlockPos pos, Axis axis) {
        BlockState blockstate = level.getBlockState(pos);
        return blockstate.isAir() || blockstate.canBeReplaced();
    }

    private void makePortalStructure(ServerLevel destLevel, BlockPos pos, Axis axis) {
        Direction direction = axis == Axis.X ? Direction.WEST : Direction.SOUTH;
        BlockState frameState = this.frameBlock.defaultBlockState();
        BlockState portalState = this.defaultBlockState().setValue(AXIS, axis);

        // First place the frame
        for (int x = -1; x < MiningPortalShape.MIN_WIDTH + 1; ++x) {
            for (int y = -1; y < MiningPortalShape.MIN_HEIGHT + 1; ++y) {
                if (x == -1 || x == MiningPortalShape.MIN_WIDTH || y == -1 || y == MiningPortalShape.MIN_HEIGHT) {
                    destLevel.setBlock(pos.relative(direction, x).above(y), frameState, 3);
                }
            }
        }

        // Then place the portal blocks
        for (int x = 0; x < MiningPortalShape.MIN_WIDTH; ++x) {
            for (int y = 0; y < MiningPortalShape.MIN_HEIGHT; ++y) {
                destLevel.setBlock(pos.relative(direction, x).above(y), portalState, 18);
            }
        }

        // Add a 3x3 platform below the portal
        Direction clockWise = direction.getClockWise();
        for (int x = -1; x < MiningPortalShape.MIN_WIDTH + 1; ++x) {
            for (int z = -1; z < 2; ++z) {
                destLevel.setBlock(pos.relative(direction, x).relative(clockWise, z).below(), frameState, 3);
            }
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Axis neighborAxis = direction.getAxis();
        Axis portalAxis = state.getValue(AXIS);
        boolean isHorizontalNeighbor = portalAxis != neighborAxis && neighborAxis.isHorizontal();
        return !isHorizontalNeighbor && !neighborState.is(this) && !(new MiningPortalShape(level, pos, portalAxis, this, this.frameBlock)).isValid()
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) {
            return state.setValue(AXIS, state.getValue(AXIS) == Axis.X ? Axis.Z : Axis.X);
        }
        return state;
    }
}
