package dev.rolypolyvole.wheatandchessboard.command

import com.mojang.brigadier.arguments.StringArgumentType
import dev.rolypolyvole.wheatandchessboard.util.Messages
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException

internal fun syncCommand() = command("sync") {
    adminOnly()

    argument("time", StringArgumentType.greedyString()) {
        runs { sync(StringArgumentType.getString(this, "time")) }
    }
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
