package com.ejafi.egmd.content.block;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.content.dimension.HostileMobBan;
import com.ejafi.egmd.content.dimension.PendingRegenerations;
import com.ejafi.egmd.core.config.ModConfig;
import com.ejafi.egmd.core.registration.ModDimensions;
import com.ejafi.egmd.core.registration.ModParticles;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

/**
 * Portal block linking the Overworld with one mining dimension. Each instance carries its target
 * dimension, frame block, and tint, so one class serves every portal variant.
 */
public class MiningPortalBlock extends Block implements Portal, LiquidBlockContainer {
    public static final EnumProperty<Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    private static final VoxelShape X_AXIS_AABB = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    private static final VoxelShape Z_AXIS_AABB = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    private static final int PARTICLE_ALPHA = 0xC8;

    private final ResourceKey<Level> dimensionKey;
    private final Block frameBlock;
    private final Holder<PoiType> poiTypeHolder;
    private final int surfaceY;
    private final int particleColor;

    public MiningPortalBlock(BlockBehaviour.Properties properties, ResourceKey<Level> dimensionKey, Block frameBlock,
                             Holder<PoiType> poiTypeHolder, int surfaceY, int tintColor) {
        super(properties);
        this.dimensionKey = dimensionKey;
        this.frameBlock = frameBlock;
        this.poiTypeHolder = poiTypeHolder;
        this.surfaceY = surfaceY;
        this.particleColor = PARTICLE_ALPHA << 24 | (tintColor & 0xFFFFFF);
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Axis.X));
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return state.getValue(AXIS) == Axis.Z ? Z_AXIS_AABB : X_AXIS_AABB;
    }

    public ResourceKey<Level> getDimensionKey() {
        return dimensionKey;
    }

    public Block getFrameBlock() {
        return frameBlock;
    }

    @Override
    protected void entityInside(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, Entity entity) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F, false);
        }

        ColorParticleOption particle = ColorParticleOption.create(ModParticles.PORTAL_SPARK.get(), this.particleColor);
        for (int i = 0; i < 4; ++i) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            double xSpeed = (random.nextFloat() - 0.5D) * 0.5D;
            double ySpeed = (random.nextFloat() - 0.5D) * 0.5D;
            double zSpeed = (random.nextFloat() - 0.5D) * 0.5D;
            int side = random.nextInt(2) * 2 - 1;
            if (!level.getBlockState(pos.west()).is(this) && !level.getBlockState(pos.east()).is(this)) {
                x = pos.getX() + 0.5D + 0.25D * side;
                xSpeed = random.nextFloat() * 2.0F * side;
            } else {
                z = pos.getZ() + 0.5D + 0.25D * side;
                zSpeed = random.nextFloat() * 2.0F * side;
            }

            level.addParticle(particle, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }

    @Override
    public @NotNull Transition getLocalTransition() {
        return Portal.Transition.CONFUSION;
    }

    @Override
    public int getPortalTransitionTime(@NotNull ServerLevel serverLevel, @NotNull Entity entity) {
        return entity instanceof Player player
                ? Math.max(1, serverLevel.getGameRules().getInt(
                        player.getAbilities().invulnerable
                                ? GameRules.RULE_PLAYERS_NETHER_PORTAL_CREATIVE_DELAY
                                : GameRules.RULE_PLAYERS_NETHER_PORTAL_DEFAULT_DELAY))
                : 0;
    }

    @Nullable
    @Override
    public DimensionTransition getPortalDestination(ServerLevel serverLevel, @NotNull Entity entity, @NotNull BlockPos pos) {
        ResourceKey<Level> resourceKey = resolveDestination(serverLevel.dimension(), this.dimensionKey);
        if (resourceKey == null || !HostileMobBan.admits(entity, resourceKey)) {
            return null;
        }
        if (PendingRegenerations.isPending(resourceKey)) {
            notifyPlayer(entity, "message.egmd.portal.regenerating");
            return null;
        }

        ServerLevel destLevel = serverLevel.getServer().getLevel(resourceKey);
        if (destLevel == null) {
            EGMD.LOGGER.warn("Portal destination dimension {} does not exist", resourceKey.location());
            return null;
        }

        WorldBorder worldBorder = destLevel.getWorldBorder();
        double scale = DimensionType.getTeleportationScale(serverLevel.dimensionType(), destLevel.dimensionType());
        BlockPos targetPos = worldBorder.clampToBounds(entity.getX() * scale, entity.getY(), entity.getZ() * scale);
        if (ModDimensions.ALL_MINING_DIMENSIONS.contains(resourceKey)) {
            targetPos = targetPos.atY(this.surfaceY);
        }

        return this.getExitPortal(destLevel, entity, pos, targetPos, worldBorder);
    }

    /**
     * Mining portals link the Overworld with their own mining dimension and nothing else, so a portal
     * standing anywhere else leads nowhere.
     *
     * @return the dimension a portal in {@code current} leads to, or null if it leads nowhere
     */
    @Nullable
    static ResourceKey<Level> resolveDestination(ResourceKey<Level> current, ResourceKey<Level> portalDimension) {
        if (current == Level.OVERWORLD) return portalDimension;
        if (current == portalDimension) return Level.OVERWORLD;
        return null;
    }

    @Nullable
    private DimensionTransition getExitPortal(ServerLevel destLevel, Entity entity, BlockPos pos, BlockPos targetPos, WorldBorder worldBorder) {
        Optional<BlockPos> optional = findPortalAround(destLevel, targetPos, worldBorder, computeSearchRadius(destLevel));
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
                EGMD.LOGGER.warn("No safe place to build a portal near {} in dimension {}", targetPos, destLevel.dimension().location());
                notifyPlayer(entity, "message.egmd.portal.no_safe_location");
                return null;
            }

            rectangle = optionalRectangle.get();
        }

        BlockState exitState = destLevel.getBlockState(rectangle.minCorner);
        Axis exitAxis = exitState.getOptionalValue(AXIS).orElse(axis);
        PortalExitPlacement placement = selectExit(destLevel, rectangle, exitAxis, entity);

        return new DimensionTransition(destLevel, placement.position(), Vec3.ZERO, placement.yRot(), 0.0F,
                DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET));
    }

    /**
     * Picks where an entity arrives at a portal: in front of it, else behind it, else inside it,
     * taking the first spot that has a floor and room for the entity.
     */
    static PortalExitPlacement selectExit(ServerLevel level, BlockUtil.FoundRectangle rectangle, Axis axis, Entity entity) {
        EntityDimensions dimensions = entity.getDimensions(entity.getPose());
        for (PortalExitPlacement candidate : List.of(
                PortalExitPlacement.computeFront(rectangle.minCorner, axis, rectangle.axis1Size),
                PortalExitPlacement.computeBack(rectangle.minCorner, axis, rectangle.axis1Size))) {
            if (isSafeArrival(level, entity, dimensions, candidate.position())) {
                return candidate;
            }
        }

        PortalExitPlacement inside = PortalExitPlacement.computeInside(rectangle.minCorner, axis, rectangle.axis1Size);
        return new PortalExitPlacement(
                PortalShape.findCollisionFreePosition(inside.position(), level, entity, dimensions), inside.yRot());
    }

    private static boolean isSafeArrival(ServerLevel level, Entity entity, EntityDimensions dimensions, Vec3 position) {
        BlockPos floor = BlockPos.containing(position).below();
        return level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
                && level.noCollision(entity, dimensions.makeBoundingBox(position));
    }

    private static int computeSearchRadius(ServerLevel destLevel) {
        return Math.max(16, (int) (ModConfig.PORTAL_SEARCH_RADIUS.getAsInt() / destLevel.dimensionType().coordinateScale()));
    }

    /**
     * Finds the nearest portal of this variant within {@code radius} blocks horizontally of
     * {@code pos}, at any height, as vanilla does for nether portals.
     */
    Optional<BlockPos> findPortalAround(ServerLevel destLevel, BlockPos pos, WorldBorder worldBorder, int radius) {
        PoiManager poiManager = destLevel.getPoiManager();
        poiManager.ensureLoadedAndValid(destLevel, pos, radius);
        return poiManager.getInSquare(
                        holder -> this.poiTypeHolder.unwrapKey().map(holder::is).orElse(false),
                        pos,
                        radius,
                        PoiManager.Occupancy.ANY)
                .map(PoiRecord::getPos)
                .filter(worldBorder::isWithinBounds)
                .filter(poiPos -> destLevel.getBlockState(poiPos).hasProperty(AXIS))
                .min(Comparator.<BlockPos>comparingDouble(poiPos -> poiPos.distSqr(pos)).thenComparingInt(BlockPos::getY));
    }

    private Optional<BlockUtil.FoundRectangle> createPortal(ServerLevel destLevel, BlockPos pos, Axis axis) {
        Optional<PortalPositionFinder.PortalSite> site = PortalPositionFinder.find(destLevel, pos, axis);
        if (site.isEmpty()) return Optional.empty();
        PortalStructureBuilder.build(destLevel, site.get().origin(), axis, this, site.get().forced());
        return Optional.of(new BlockUtil.FoundRectangle(site.get().origin(), MiningPortalShape.MIN_WIDTH, MiningPortalShape.MIN_HEIGHT));
    }

    private static void notifyPlayer(Entity entity, String translationKey) {
        if (entity instanceof ServerPlayer player) {
            player.displayClientMessage(Component.translatable(translationKey), true);
        }
    }

    @Override
    public boolean canPlaceLiquid(@Nullable Player player, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Fluid fluid) {
        return false;
    }

    @Override
    public boolean placeLiquid(@NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull FluidState fluidState) {
        return false;
    }

    @Override
    protected boolean canBeReplaced(@NotNull BlockState state, @NotNull Fluid fluid) {
        return false;
    }

    @Override
    protected @NotNull BlockState updateShape(BlockState state, Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        Axis neighborAxis = direction.getAxis();
        Axis portalAxis = state.getValue(AXIS);
        boolean isHorizontalNeighbor = portalAxis != neighborAxis && neighborAxis.isHorizontal();
        return !isHorizontalNeighbor && !neighborState.is(this) && !(new MiningPortalShape(level, pos, portalAxis, this)).isComplete()
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    protected @NotNull BlockState rotate(@NotNull BlockState state, @NotNull Rotation rotation) {
        if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) {
            return state.setValue(AXIS, state.getValue(AXIS) == Axis.X ? Axis.Z : Axis.X);
        }
        return state;
    }
}
