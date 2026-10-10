package com.ejafi.egmd.content.command;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.content.dimension.PendingRegenerations;
import com.ejafi.egmd.content.dimension.PlayerRelocator;
import com.ejafi.egmd.core.registration.ModDimensions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Handles registration and execution of EGMD commands.
 */
@EventBusSubscriber(modid = EGMD.MODID)
public class ModCommands {
    private ModCommands() {
        /* This utility class should not be instantiated */
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    /**
     * Registers EGMD commands to the dispatcher.
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("egmd")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("regenerate")
                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                .executes(ModCommands::regenerateDimension))));
    }

    /**
     * Closes a mining dimension's portals until the server stops, evacuates its players to the
     * Overworld, and provides instructions for manual regeneration.
     */
    private static int regenerateDimension(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerLevel targetLevel = DimensionArgument.getDimension(context, "dimension");
        ResourceKey<Level> dimensionKey = targetLevel.dimension();

        if (!ModDimensions.ALL_MINING_DIMENSIONS.contains(dimensionKey)) {
            context.getSource().sendFailure(Component.translatable("command.egmd.regenerate.failure.not_egmd"));
            return 0;
        }

        PendingRegenerations.mark(dimensionKey);
        List<ServerPlayer> playersInDim = List.copyOf(targetLevel.players());
        EGMD.LOGGER.info("Evacuating {} players from {}", playersInDim.size(), dimensionKey.location());

        int stranded = 0;
        for (ServerPlayer player : playersInDim) {
            if (PlayerRelocator.moveToOverworldSpawn(player)) {
                player.sendSystemMessage(Component.translatable("command.egmd.regenerate.player_moved"));
            } else {
                EGMD.LOGGER.warn("Could not move {} out of {}", player.getGameProfile().getName(), dimensionKey.location());
                stranded++;
            }
        }

        if (stranded > 0) {
            context.getSource().sendFailure(Component.translatable("command.egmd.regenerate.failure.not_evacuated",
                    stranded, dimensionKey.location().toString()));
            return 0;
        }

        // Direct folder deletion while running is unsafe, so we provide clear manual instructions.
        String folderPath = DimensionType.getStorageFolder(dimensionKey,
                context.getSource().getServer().getWorldPath(LevelResource.ROOT)).normalize().toString();

        context.getSource().sendSuccess(() -> Component.translatable("command.egmd.regenerate.success.evacuated",
                playersInDim.size(), dimensionKey.location().toString()), true);
        context.getSource().sendSuccess(() -> Component.translatable("command.egmd.regenerate.success.instructions.title"), true);
        context.getSource().sendSuccess(() -> Component.translatable("command.egmd.regenerate.success.instructions.step1"), true);
        context.getSource().sendSuccess(() -> Component.translatable("command.egmd.regenerate.success.instructions.step2", folderPath), true);
        context.getSource().sendSuccess(() -> Component.translatable("command.egmd.regenerate.success.instructions.step3"), true);

        return 1;
    }
}
