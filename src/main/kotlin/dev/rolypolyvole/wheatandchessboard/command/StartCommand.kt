package dev.rolypolyvole.wheatandchessboard.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import dev.rolypolyvole.wheatandchessboard.config.ModConfig

internal fun startCommand() = command("start") {
    adminOnly()
    runs { start(1) }

    argument("day", IntegerArgumentType.integer(1)) {
        runs { start(IntegerArgumentType.getInteger(this, "day")) }
    }
}

private fun Context.start(day: Int) {
    val totalDays = ModConfig.config.totalDays

    if (day > totalDays) fail("The challenge only has $totalDays days.")
    if (!challenge.start(day)) fail("The challenge is already running.")

    val players = challenge.state.players.size
    val hint = if (players == 0) "Nobody is in it yet, add players with /wheatandchess add." else "$players player(s) are in it."

    reply("Started the challenge on day $day. $hint")
}
