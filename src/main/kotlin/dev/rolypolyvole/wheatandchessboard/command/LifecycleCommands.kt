package dev.rolypolyvole.wheatandchessboard.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import dev.rolypolyvole.wheatandchessboard.config.ModConfig
import dev.rolypolyvole.wheatandchessboard.util.Messages
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException

internal fun startCommand() = command("start") {
    adminOnly()
    runs { start(1) }

    argument("day", IntegerArgumentType.integer(1)) {
        runs { start(IntegerArgumentType.getInteger(this, "day")) }
    }
}

internal fun endCommand() = command("end") {
    adminOnly()

    runs {
        if (!challenge.end()) fail("No challenge is running.")
        reply("Ended the challenge.")
    }
}

internal fun syncCommand() = command("sync") {
    adminOnly()

    argument("time", StringArgumentType.greedyString()) {
        runs { sync(StringArgumentType.getString(this, "time")) }
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

private fun Context.sync(time: String) {
    val now = LocalDateTime.now()
    val target = parseTime(time.trim(), now) ?: fail("Use a time like 00:00 or a date and time like 2026-11-12T00:00.")
    val timeLeft = Duration.between(now, target).toMillis()

    if (timeLeft <= 0) fail("That time is in the past.")
    if (!challenge.syncDayEnd(timeLeft)) fail("No challenge is running.")

    reply("Day ${challenge.state.day} now ends in ${Messages.duration(timeLeft)}.")
}

private fun parseTime(time: String, now: LocalDateTime): LocalDateTime? {
    try {
        val today = now.toLocalDate().atTime(LocalTime.parse(time))
        return if (today.isAfter(now)) today else today.plusDays(1)
    } catch (_: DateTimeParseException) {
    }

    return try {
        LocalDateTime.parse(time)
    } catch (_: DateTimeParseException) {
        null
    }
}
