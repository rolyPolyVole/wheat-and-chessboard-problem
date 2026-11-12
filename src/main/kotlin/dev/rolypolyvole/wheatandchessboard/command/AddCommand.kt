package dev.rolypolyvole.wheatandchessboard.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.rolypolyvole.wheatandchessboard.game.Challenge
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.LEVEL_GAMEMASTERS
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.hasPermission
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

class AddCommand {
    fun build(): LiteralArgumentBuilder<CommandSourceStack> {
        return literal("add")
            .requires(hasPermission(LEVEL_GAMEMASTERS))
            .then(
                literal("all")
                    .executes { add(it.source, it.source.server.playerList.players) }
            )
            .then(
                argument("players", EntityArgument.players())
                    .executes { add(it.source, EntityArgument.getPlayers(it, "players")) }
            )
    }

    private fun add(source: CommandSourceStack, players: Collection<ServerPlayer>): Int {
        val challenge = Challenge.current ?: return 0
        val added = players.count { challenge.add(it) }

        if (added == 0) {
            source.sendFailure(Component.literal("Those players are already in the challenge."))
            return 0
        }

        source.sendSuccess({ Component.literal("Added $added player(s) to the challenge.") }, true)
        return Command.SINGLE_SUCCESS
    }
}
