package net.caduzz.futuremod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.caduzz.futuremod.block.ModBlocks;
import net.caduzz.futuremod.dimension.ModDimensions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Comandos do mod, incluindo teleporte para a dimensão criativa.
 */
public final class ModCommands {

    private ModCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("creative_realm")
                        .requires(source -> source.getEntity() instanceof ServerPlayer)
                        .executes(ModCommands::goToCreativeRealm)
        );
        dispatcher.register(
                Commands.literal("realm")
                        .requires(source -> source.getEntity() instanceof ServerPlayer)
                        .executes(ModCommands::goToCreativeRealm)
        );
        dispatcher.register(
                Commands.literal("boards_test")
                        .requires(source -> source.getEntity() instanceof ServerPlayer)
                        .executes(ModCommands::giveBoardsTestItems)
        );
    }

    private static int goToCreativeRealm(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = (ServerPlayer) context.getSource().getEntity();
        if (player == null) return 0;

        ServerLevel creativeRealm = ModDimensions.getOrCreateCreativeRealm(player.getServer());
        if (creativeRealm == null) {
            context.getSource().sendFailure(Component.literal("Dimensão Creative Realm não está disponível neste mundo."));
            return 0;
        }

        if (player.level().dimension() == ModDimensions.CREATIVE_REALM_LEVEL) {
            ServerLevel overworld = player.getServer().getLevel(Level.OVERWORLD);
            if (overworld != null) {
                ModDimensions.teleportToDimension(player, overworld);
                context.getSource().sendSuccess(() -> Component.literal("Teleportado para o Overworld."), true);
            } else {
                context.getSource().sendFailure(Component.literal("Overworld não encontrado."));
                return 0;
            }
        } else {
            ModDimensions.teleportToDimension(player, creativeRealm);
            context.getSource().sendSuccess(() -> Component.literal("Teleportado para a Creative Realm."), true);
        }
        return 1;
    }

    private static int giveBoardsTestItems(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = (ServerPlayer) context.getSource().getEntity();
        if (player == null) return 0;
        player.getInventory().add(new ItemStack(ModBlocks.CHECKERS_BLOCK.get()));
        player.getInventory().add(new ItemStack(ModBlocks.CHESS_BLOCK.get()));
        context.getSource().sendSuccess(() -> Component.literal("Recebeste tabuleiro de damas e tabuleiro de xadrez."), true);
        return 1;
    }
}
