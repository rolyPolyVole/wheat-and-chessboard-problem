package dev.rolypolyvole.wheatandchessboard.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.rolypolyvole.wheatandchessboard.config.ModConfig
import dev.rolypolyvole.wheatandchessboard.game.Challenge
import dev.rolypolyvole.wheatandchessboard.util.Messages
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

        val progress = source.player?.let { state.players[it.uuid] }

        if (progress == null) {
            source.sendFailure(Component.literal("You are not in the challenge."))
            return 0
        }

        val messages = ModConfig.config.messages
        val placeholders = challenge.placeholders(progress)

        send(source, messages?.info, placeholders)

        if (progress.eliminated) send(source, messages?.infoEliminated, placeholders)

        return Command.SINGLE_SUCCESS
    }

    private fun send(source: CommandSourceStack, template: String?, placeholders: Map<String, Any>) {
        Messages.format(template, placeholders)?.let { message -> source.sendSuccess({ message }, false) }
    }
}
