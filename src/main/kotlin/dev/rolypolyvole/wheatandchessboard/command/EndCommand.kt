package dev.rolypolyvole.wheatandchessboard.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.rolypolyvole.wheatandchessboard.game.Challenge
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.LEVEL_GAMEMASTERS
import net.minecraft.commands.Commands.hasPermission
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component

class EndCommand {
    fun build(): LiteralArgumentBuilder<CommandSourceStack> {
        return literal("end")
            .requires(hasPermission(LEVEL_GAMEMASTERS))
            .executes { end(it.source, false) }
            .then(
                literal("abort")
                    .executes { end(it.source, true) }
            )
    }

    private fun end(source: CommandSourceStack, abort: Boolean): Int {
        val challenge = Challenge.current ?: return 0

        if (!challenge.end(abort)) {
            source.sendFailure(Component.literal("No challenge is running."))
            return 0
        }

        val reply = if (abort) "Aborted the challenge." else "Ended the challenge."

        source.sendSuccess({ Component.literal(reply) }, true)
        return Command.SINGLE_SUCCESS
    }
}
