package com.ejafi.egmd.dimension;

import com.mojang.logging.LogUtils;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

final class DimensionRegenerator {
    private static final Logger LOGGER = LogUtils.getLogger();

    private DimensionRegenerator() {}

    static RegenerationResult regenerate(ServerLevel level) {
        ResourceKey<Level> dimension = level.dimension();
        if (MiningDimension.findByLevelKey(dimension).isEmpty()) {
            return new RegenerationResult.NotMiningDimension();
        }

        PendingRegenerations.mark(dimension);
        List<ServerPlayer> players = List.copyOf(level.players());
        LOGGER.info("Evacuating {} players from {}", players.size(), dimension.location());

        int strandedCount = 0;
        for (ServerPlayer player : players) {
            switch (PlayerRelocator.sendToOverworld(player, DimensionMessages.PLAYER_MOVED)) {
                case OverworldMove.Arrived arrived -> {}
                case OverworldMove.Canceled canceled -> {
                    LOGGER.warn(
                            "Could not move {} out of {}",
                            player.getGameProfile().getName(),
                            dimension.location());
                    strandedCount++;
                }
            }
        }

        if (strandedCount > 0) {
            return new RegenerationResult.PlayersStranded(strandedCount);
        }

        Path storageFolder = DimensionType.getStorageFolder(
                        dimension, level.getServer().getWorldPath(LevelResource.ROOT))
                .normalize();
        return new RegenerationResult.Evacuated(players.size(), storageFolder);
    }
}
