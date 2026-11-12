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
            .executes { end(it.source) }
    }

    private fun end(source: CommandSourceStack): Int {
        val challenge = Challenge.current ?: return 0

        if (!challenge.end()) {
            source.sendFailure(Component.literal("No challenge is running."))
            return 0
        }

        source.sendSuccess({ Component.literal("Ended the challenge.") }, true)
        return Command.SINGLE_SUCCESS
    }
}
