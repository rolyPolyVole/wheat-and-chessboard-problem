package dev.rolypolyvole.wheatandchessboard.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType.getInteger
import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.rolypolyvole.wheatandchessboard.config.ModConfig
import dev.rolypolyvole.wheatandchessboard.game.Challenge
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.LEVEL_GAMEMASTERS
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.hasPermission
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component

class StartCommand {
    fun build(): LiteralArgumentBuilder<CommandSourceStack> {
        return literal("start")
            .requires(hasPermission(LEVEL_GAMEMASTERS))
            .executes { start(it.source, 1) }
            .then(
                argument("day", integer(1))
                    .executes { start(it.source, getInteger(it, "day")) }
            )
    }

    private fun start(source: CommandSourceStack, day: Int): Int {
        val challenge = Challenge.current ?: return 0
        val totalDays = ModConfig.config.totalDays

        if (day > totalDays) {
            source.sendFailure(Component.literal("The challenge only has $totalDays days."))
            return 0
        }

        if (!challenge.start(day)) {
            source.sendFailure(Component.literal("The challenge is already running."))
            return 0
        }

        val players = challenge.state.players.size
        val hint = if (players == 0) "Nobody is in it yet, add players with /wheatandchess add." else "$players player(s) are in it."

        source.sendSuccess({ Component.literal("Started the challenge on day $day. $hint") }, true)
        return Command.SINGLE_SUCCESS
    }
}
