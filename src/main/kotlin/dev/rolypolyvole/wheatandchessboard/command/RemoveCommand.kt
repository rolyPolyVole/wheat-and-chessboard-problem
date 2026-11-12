package dev.rolypolyvole.wheatandchessboard.command

import com.mojang.brigadier.arguments.StringArgumentType
import dev.rolypolyvole.wheatandchessboard.game.Challenge
import net.minecraft.commands.SharedSuggestionProvider

internal fun removeCommand() = command("remove") {
    adminOnly()

    argument("player", StringArgumentType.word()) {
        suggests { _, builder -> SharedSuggestionProvider.suggest(participantNames(), builder) }
        runs { remove(StringArgumentType.getString(this, "player")) }
    }
}

private fun Context.remove(name: String) {
    if (!challenge.remove(name)) fail("$name is not in the challenge.")
    reply("Removed $name from the challenge.")
}

private fun participantNames(): List<String> {
    return Challenge.current?.state?.players?.values?.map { it.name } ?: listOf()
}
