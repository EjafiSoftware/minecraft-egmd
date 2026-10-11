package com.ejafi.egmd.portal;

import com.ejafi.egmd.dimension.HostileMobGuard;
import com.ejafi.egmd.dimension.MiningDimension;
import com.ejafi.egmd.dimension.PendingRegenerations;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public final class MiningPortalBlock extends Block implements Portal, LiquidBlockContainer {
    public static final EnumProperty<Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final VoxelShape X_AXIS_PANE = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    private static final VoxelShape Z_AXIS_PANE = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);
    private static final int PARTICLE_ALPHA = 0xC8;

    private final PortalVariant variant;
    private final int particleColor;

    MiningPortalBlock(BlockBehaviour.Properties properties, PortalVariant variant) {
        super(properties);
        this.variant = variant;
        this.particleColor = PARTICLE_ALPHA << 24 | (variant.tintColor() & 0xFFFFFF);
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Axis.X));
    }

    public static VoxelShape getPaneShape(Axis axis) {
        return axis == Axis.Z ? Z_AXIS_PANE : X_AXIS_PANE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getPaneShape(state.getValue(AXIS));
    }

    ResourceKey<Level> getDimensionKey() {
        return variant.dimension().levelKey();
    }

    Block getFrameBlock() {
        return variant.frameBlock();
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D,
                    SoundEvents.PORTAL_AMBIENT,
                    SoundSource.BLOCKS,
                    0.5F,
                    random.nextFloat() * 0.4F + 0.8F,
                    false);
        }

        ColorParticleOption particle =
                ColorParticleOption.create(PortalRegisters.PORTAL_SPARK.get(), this.particleColor);
        boolean emitsAlongX = state.getValue(AXIS) == Axis.Z;
        for (int i = 0; i < 4; ++i) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            double xSpeed = (random.nextFloat() - 0.5D) * 0.5D;
            double ySpeed = (random.nextFloat() - 0.5D) * 0.5D;
            double zSpeed = (random.nextFloat() - 0.5D) * 0.5D;
            int side = random.nextInt(2) * 2 - 1;
            if (emitsAlongX) {
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
    public Transition getLocalTransition() {
        return Portal.Transition.CONFUSION;
    }

    @Override
    public int getPortalTransitionTime(ServerLevel serverLevel, Entity entity) {
        return entity instanceof Player player
                ? Math.max(
                        1,
                        serverLevel
                                .getGameRules()
                                .getInt(
                                        player.getAbilities().invulnerable
                                                ? GameRules.RULE_PLAYERS_NETHER_PORTAL_CREATIVE_DELAY
                                                : GameRules.RULE_PLAYERS_NETHER_PORTAL_DEFAULT_DELAY))
                : 0;
    }

    @Override
    public @Nullable DimensionTransition getPortalDestination(ServerLevel serverLevel, Entity entity, BlockPos pos) {
        ResourceKey<Level> destination;
        switch (chooseRoute(entity, getDimensionKey())) {
            case PortalRoute.Travel travel -> destination = travel.destination();
            case PortalRoute.Unlinked unlinked -> {
                return null;
            }
            case PortalRoute.Refused refused -> {
                return null;
            }
            case PortalRoute.ClosedForRegeneration closedForRegeneration -> {
                notifyPlayer(entity, PortalMessages.CLOSED_FOR_REGENERATION);
                return null;
            }
        }

        ServerLevel destLevel = serverLevel.getServer().getLevel(destination);
        if (destLevel == null) {
            LOGGER.warn("Portal destination dimension {} does not exist", destination.location());
            return null;
        }

        double scale = DimensionType.getTeleportationScale(serverLevel.dimensionType(), destLevel.dimensionType());
        BlockPos scaledPos =
                destLevel.getWorldBorder().clampToBounds(entity.getX() * scale, entity.getY(), entity.getZ() * scale);
        Axis sourceAxis = serverLevel.getBlockState(pos).getOptionalValue(AXIS).orElse(Axis.X);
        ArrivalTarget target = computeArrivalTarget(new PortalFootprint(scaledPos, sourceAxis), destination);

        Optional<BlockUtil.FoundRectangle> exitPortal = new ExitPortalFinder(destLevel, variant).findOrCreate(target);
        if (exitPortal.isEmpty()) {
            LOGGER.warn(
                    "No safe place to build a portal near {} in dimension {}",
                    target.footprint().origin(),
                    destLevel.dimension().location());
            notifyPlayer(entity, PortalMessages.NO_SAFE_LOCATION);
            return null;
        }

        BlockUtil.FoundRectangle rectangle = exitPortal.orElseThrow();
        Axis exitAxis = destLevel
                .getBlockState(rectangle.minCorner)
                .getOptionalValue(AXIS)
                .orElse(sourceAxis);
        PortalExit exit = new PortalExitSelector(destLevel, entity).select(rectangle, exitAxis);
        return new DimensionTransition(
                destLevel,
                exit.position(),
                Vec3.ZERO,
                exit.yRot(),
                0.0F,
                DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET));
    }

    static PortalRoute chooseRoute(Entity traveler, ResourceKey<Level> portalDimension) {
        ResourceKey<Level> destination = resolveDestination(traveler.level().dimension(), portalDimension);
        if (destination == null) {
            return new PortalRoute.Unlinked();
        }

        if (!HostileMobGuard.admits(traveler, destination)) {
            return new PortalRoute.Refused();
        }

        if (PendingRegenerations.isPending(destination)) {
            return new PortalRoute.ClosedForRegeneration();
        }

        return new PortalRoute.Travel(destination);
    }

    static @Nullable ResourceKey<Level> resolveDestination(
            ResourceKey<Level> current, ResourceKey<Level> portalDimension) {
        if (current.equals(Level.OVERWORLD)) {
            return portalDimension;
        }
        if (current.equals(portalDimension)) {
            return Level.OVERWORLD;
        }
        return null;
    }

    static ArrivalTarget computeArrivalTarget(PortalFootprint scaled, ResourceKey<Level> destination) {
        return MiningDimension.findByLevelKey(destination)
                .<ArrivalTarget>map(mining -> new ArrivalTarget.OnFlatSurface(
                        new PortalFootprint(scaled.origin().atY(mining.computeSurfaceY()), scaled.axis())))
                .orElseGet(() -> new ArrivalTarget.OnTerrain(scaled));
    }

    private static void notifyPlayer(Entity entity, String translationKey) {
        if (entity instanceof ServerPlayer player) {
            player.displayClientMessage(Component.translatable(translationKey), true);
        }
    }

    @Override
    public boolean canPlaceLiquid(
            @Nullable Player player, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        return false;
    }

    @Override
    protected boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos) {
        Axis axis = state.getValue(AXIS);
        return !isAcrossPlane(axis, direction)
                        && !neighborState.is(this)
                        && !new MiningPortalShape(level, pos, axis, this).isComplete()
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    static boolean isAcrossPlane(Axis portalAxis, Direction direction) {
        Axis neighborAxis = direction.getAxis();
        return neighborAxis.isHorizontal() && neighborAxis != portalAxis;
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

    sealed interface PortalRoute {
        record Travel(ResourceKey<Level> destination) implements PortalRoute {}

        record Unlinked() implements PortalRoute {}

        record Refused() implements PortalRoute {}

        record ClosedForRegeneration() implements PortalRoute {}
    }
}
