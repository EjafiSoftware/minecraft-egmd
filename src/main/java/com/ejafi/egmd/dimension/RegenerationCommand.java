package com.ejafi.egmd.dimension;

import com.ejafi.egmd.EGMD;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = EGMD.MOD_ID)
public final class RegenerationCommand {
    private static final String DIMENSION_ARGUMENT = "dimension";
    private static final int FAILURE = 0;

    private RegenerationCommand() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("egmd")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("regenerate")
                        .then(Commands.argument(DIMENSION_ARGUMENT, DimensionArgument.dimension())
                                .executes(RegenerationCommand::regenerateDimension))));
    }

    private static int regenerateDimension(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerLevel level = DimensionArgument.getDimension(context, DIMENSION_ARGUMENT);
        CommandSourceStack source = context.getSource();
        String dimensionName = level.dimension().location().toString();

        return switch (DimensionRegenerator.regenerate(level)) {
            case RegenerationResult.NotMiningDimension notMiningDimension ->
                reportFailure(source, Component.translatable(DimensionMessages.NOT_MINING_DIMENSION));
            case RegenerationResult.PlayersStranded(int strandedCount) ->
                reportFailure(
                        source,
                        Component.translatable(DimensionMessages.PLAYERS_STRANDED, strandedCount, dimensionName));
            case RegenerationResult.Evacuated(int evacuatedCount, Path storageFolder) ->
                reportSuccess(
                        source,
                        List.of(
                                Component.translatable(DimensionMessages.EVACUATED, evacuatedCount, dimensionName),
                                Component.translatable(DimensionMessages.INSTRUCTIONS_TITLE),
                                Component.translatable(DimensionMessages.INSTRUCTIONS_STOP_SERVER),
                                Component.translatable(
                                        DimensionMessages.INSTRUCTIONS_DELETE_FOLDER, storageFolder.toString()),
                                Component.translatable(DimensionMessages.INSTRUCTIONS_RESTART_SERVER)));
        };
    }

    private static int reportFailure(CommandSourceStack source, Component message) {
        source.sendFailure(message);
        return FAILURE;
    }

    private static int reportSuccess(CommandSourceStack source, List<Component> lines) {
        lines.forEach(line -> source.sendSuccess(() -> line, true));
        return Command.SINGLE_SUCCESS;
    }
}
