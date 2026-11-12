package dev.rolypolyvole.wheatandchessboard.command

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

private fun Context.add(players: Collection<ServerPlayer>) {
    val added = players.count { challenge.add(it) }

    if (added == 0) fail("Those players are already in the challenge.")
    reply("Added $added player(s) to the challenge.")
}
