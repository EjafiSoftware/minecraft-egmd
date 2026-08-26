package com.ejafi.egmd.content.command;

import com.ejafi.egmd.EGMD;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.core.BlockPos;
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

import java.util.List;

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
     * Evacuates players from a mining dimension and provides instructions for manual regeneration.
     */
    private static int regenerateDimension(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerLevel targetLevel = DimensionArgument.getDimension(context, "dimension");
        ResourceKey<Level> dimensionKey = targetLevel.dimension();

        if (!dimensionKey.location().getNamespace().equals(EGMD.MODID)) {
            context.getSource().sendFailure(Component.translatable("command.egmd.regenerate.failure.not_egmd"));
            return 0;
        }

        ServerLevel overworld = context.getSource().getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            context.getSource().sendFailure(Component.translatable("command.egmd.regenerate.failure.no_overworld"));
            return 0;
        }

        List<ServerPlayer> playersInDim = List.copyOf(targetLevel.players());
        EGMD.LOGGER.info("Evacuating {} players from {}", playersInDim.size(), dimensionKey.location());

        for (ServerPlayer player : playersInDim) {
            player.setPortalCooldown();
            BlockPos spawnPos = overworld.getSharedSpawnPos();
            player.teleportTo(overworld,
                    spawnPos.getX() + 0.5,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5,
                    player.getYRot(),
                    player.getXRot()
            );
            player.sendSystemMessage(Component.translatable("command.egmd.regenerate.player_moved"));
        }

        // Direct folder deletion while running is unsafe, so we provide clear manual instructions.
        String folderPath = DimensionType.getStorageFolder(dimensionKey,
                context.getSource().getServer().getWorldPath(LevelResource.ROOT)).normalize().toString();

        context.getSource().sendSuccess(() -> Component.translatable("command.egmd.regenerate.success.evacuated", dimensionKey.location().toString()), true);
        context.getSource().sendSuccess(() -> Component.translatable("command.egmd.regenerate.success.instructions.title"), true);
        context.getSource().sendSuccess(() -> Component.translatable("command.egmd.regenerate.success.instructions.step1"), true);
        context.getSource().sendSuccess(() -> Component.translatable("command.egmd.regenerate.success.instructions.step2", folderPath), true);
        context.getSource().sendSuccess(() -> Component.translatable("command.egmd.regenerate.success.instructions.step3"), true);

        return 1;
    }
}
