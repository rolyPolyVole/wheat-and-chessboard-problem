package dev.rolypolyvole.wheatandchessboard.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.word
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.rolypolyvole.wheatandchessboard.game.Challenge
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.LEVEL_GAMEMASTERS
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.hasPermission
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component

class RemoveCommand {
    fun build(): LiteralArgumentBuilder<CommandSourceStack> {
        return literal("remove")
            .requires(hasPermission(LEVEL_GAMEMASTERS))
            .then(
                argument("player", word())
                    .suggests { _, builder -> SharedSuggestionProvider.suggest(participantNames(), builder) }
                    .executes { remove(it.source, getString(it, "player")) }
            )
    }

    private fun remove(source: CommandSourceStack, name: String): Int {
        val challenge = Challenge.current ?: return 0

        if (!challenge.remove(name)) {
            source.sendFailure(Component.literal("$name is not in the challenge."))
            return 0
        }

        source.sendSuccess({ Component.literal("Removed $name from the challenge.") }, true)
        return Command.SINGLE_SUCCESS
    }

    private fun participantNames(): List<String> {
        return Challenge.current?.state?.players?.values?.map { it.name } ?: listOf()
    }
}
