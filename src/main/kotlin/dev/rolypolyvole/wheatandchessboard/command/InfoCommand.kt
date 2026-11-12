package dev.rolypolyvole.wheatandchessboard.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.rolypolyvole.wheatandchessboard.config.ModConfig
import dev.rolypolyvole.wheatandchessboard.game.Challenge
import dev.rolypolyvole.wheatandchessboard.util.Messages
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component

class InfoCommand {
    fun build(): LiteralArgumentBuilder<CommandSourceStack> {
        return literal("info")
            .executes { info(it.source) }
    }

    private fun info(source: CommandSourceStack): Int {
        val challenge = Challenge.current ?: return 0
        val state = challenge.state

        if (!state.active) {
            source.sendFailure(Component.literal("No challenge is running."))
            return 0
        }

        val remaining = state.players.values.count { !it.eliminated }
        val timeLeft = Messages.duration(challenge.timeLeftMillis)

        source.sendSuccess({
            Component.literal("Day ${state.day} of ${ModConfig.config.totalDays}, $timeLeft left. ")
                .append(Component.literal("$remaining of ${state.players.size} players remain."))
                .withStyle(ChatFormatting.GOLD)
        }, false)

        val progress = source.player?.let { state.players[it.uuid] } ?: return Command.SINGLE_SUCCESS

        if (progress.eliminated) {
            source.sendSuccess({ Component.literal("You are out of the challenge.").withStyle(ChatFormatting.RED) }, false)
        } else {
            source.sendSuccess({ Messages.progress(progress.collected, challenge.quota) }, false)
        }

        return Command.SINGLE_SUCCESS
    }
}
