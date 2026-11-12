package dev.rolypolyvole.wheatandchessboard.command

import com.mojang.brigadier.arguments.StringArgumentType
import dev.rolypolyvole.wheatandchessboard.game.Challenge
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.server.level.ServerPlayer

internal fun addCommand() = command("add") {
    adminOnly()

    then(command("all") {
        runs { add(source.server.playerList.players) }
    })

    argument("players", EntityArgument.players()) {
        runs { add(EntityArgument.getPlayers(this, "players")) }
    }
}

internal fun removeCommand() = command("remove") {
    adminOnly()

    argument("player", StringArgumentType.word()) {
        suggests { _, builder -> SharedSuggestionProvider.suggest(participantNames(), builder) }
        runs { remove(StringArgumentType.getString(this, "player")) }
    }
}

private fun Context.add(players: Collection<ServerPlayer>) {
    val added = players.count { challenge.add(it) }

    if (added == 0) fail("Those players are already in the challenge.")
    reply("Added $added player(s) to the challenge.")
}

private fun Context.remove(name: String) {
    if (!challenge.remove(name)) fail("$name is not in the challenge.")
    reply("Removed $name from the challenge.")
}

private fun participantNames(): List<String> {
    return Challenge.current?.state?.players?.values?.map { it.name } ?: listOf()
}
