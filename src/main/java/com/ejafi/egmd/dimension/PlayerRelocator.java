package com.ejafi.egmd.dimension;

import com.ejafi.egmd.Egmd;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Egmd.MOD_ID)
public final class PlayerRelocator {
    private PlayerRelocator() {}

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        switch (chooseRescue(player.level().dimension(), () -> assessFooting(player))) {
            case LoginRescue.Stay stay -> {}
            case LoginRescue.LiftToClearSpace(BlockPos clearSpace) -> {
                Vec3 target = Vec3.atBottomCenterOf(clearSpace);
                player.teleportTo(target.x, target.y, target.z);
                notifyRelocated(player);
            }
            case LoginRescue.SendToOverworld sendToOverworld -> sendToOverworld(player);
        }
    }

    static LoginRescue chooseRescue(ResourceKey<Level> dimension, Supplier<Footing> footing) {
        boolean flat = EgmdDimensions.find(dimension)
                .map(egmd -> egmd.terrain() instanceof Terrain.Flat)
                .orElse(false);
        if (!flat) {
            return new LoginRescue.Stay();
        }

        return switch (footing.get()) {
            case Footing.Standing standing -> new LoginRescue.Stay();
            case Footing.Buried(BlockPos clearSpace) -> new LoginRescue.LiftToClearSpace(clearSpace);
            case Footing.Entombed entombed -> new LoginRescue.SendToOverworld();
        };
    }

    private static Footing assessFooting(ServerPlayer player) {
        if (!player.isInWall()) {
            return new Footing.Standing();
        }

        return new ClearSpaceFinder(player.serverLevel(), player)
                .findAbove(player.blockPosition())
                .<Footing>map(Footing.Buried::new)
                .orElseGet(Footing.Entombed::new);
    }

    private static void sendToOverworld(ServerPlayer player) {
        ServerLevel overworld = player.server.overworld();
        BlockPos surface =
                overworld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, overworld.getSharedSpawnPos());
        Vec3 target = Vec3.atBottomCenterOf(
                new ClearSpaceFinder(overworld, player).findAbove(surface).orElse(surface));
        player.setPortalCooldown();
        player.teleportTo(overworld, target.x, target.y, target.z, player.getYRot(), player.getXRot());
        if (player.level().equals(overworld)) {
            notifyRelocated(player);
        }
    }

    private static void notifyRelocated(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable(DimensionMessages.LOGIN_RELOCATED));
    }

    sealed interface Footing {
        record Standing() implements Footing {}

        record Buried(BlockPos clearSpace) implements Footing {}

        record Entombed() implements Footing {}
    }

    sealed interface LoginRescue {
        record Stay() implements LoginRescue {}

        record LiftToClearSpace(BlockPos clearSpace) implements LoginRescue {}

        record SendToOverworld() implements LoginRescue {}
    }
}
