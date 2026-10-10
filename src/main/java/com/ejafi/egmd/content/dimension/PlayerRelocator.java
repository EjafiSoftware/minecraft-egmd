package com.ejafi.egmd.content.dimension;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.core.registration.ModDimensions;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Moves players somewhere they can stand: out of a mining dimension closed for regeneration, or
 * out of terrain that changed around them while they were offline.
 */
@EventBusSubscriber(modid = EGMD.MODID)
public final class PlayerRelocator {
    private PlayerRelocator() {}

    /**
     * Moves {@code player} to the Overworld spawn, standing clear of any blocks there.
     *
     * @return {@code false} if the player is still outside the Overworld, such as when another mod
     *         canceled the dimension change
     */
    public static boolean moveToOverworldSpawn(ServerPlayer player) {
        ServerLevel overworld = player.server.overworld();
        BlockPos surface = overworld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, overworld.getSharedSpawnPos());
        BlockPos standing = findClearSpace(overworld, player, surface).orElse(surface);
        player.setPortalCooldown();
        player.teleportTo(overworld, standing.getX() + 0.5, standing.getY(), standing.getZ() + 0.5, player.getYRot(), player.getXRot());
        return player.level() == overworld;
    }

    /**
     * Rescues a player who logs in to a mining dimension that is closed for regeneration, or who
     * would suffocate because the terrain around them was regenerated while they were away.
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ResourceKey<Level> dimension = player.level().dimension();
        if (!ModDimensions.ALL_MINING_DIMENSIONS.contains(dimension)) return;

        if (PendingRegenerations.isPending(dimension)) {
            if (moveToOverworldSpawn(player)) {
                player.sendSystemMessage(Component.translatable("message.egmd.login.relocated"));
            }
            return;
        }

        if (!player.isInWall()) return;
        Optional<BlockPos> clear = findClearSpace(player.serverLevel(), player, player.blockPosition());
        if (clear.isPresent()) {
            BlockPos pos = clear.get();
            player.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        } else if (!moveToOverworldSpawn(player)) {
            return;
        }
        player.sendSystemMessage(Component.translatable("message.egmd.login.relocated"));
    }

    /**
     * Returns the lowest position at or above {@code start} where {@code entity} fits without
     * colliding, or empty if there is none below the build limit.
     */
    static Optional<BlockPos> findClearSpace(ServerLevel level, Entity entity, BlockPos start) {
        EntityDimensions dimensions = entity.getDimensions(entity.getPose());
        int highestFeetY = level.getMaxBuildHeight() - Mth.ceil(dimensions.height());
        for (BlockPos pos = start; pos.getY() <= highestFeetY; pos = pos.above()) {
            if (level.noCollision(entity, dimensions.makeBoundingBox(Vec3.atBottomCenterOf(pos)))) {
                return Optional.of(pos);
            }
        }
        return Optional.empty();
    }
}
