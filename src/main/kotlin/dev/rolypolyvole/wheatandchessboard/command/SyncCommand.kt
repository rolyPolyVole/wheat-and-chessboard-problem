package dev.rolypolyvole.wheatandchessboard.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.greedyString
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.rolypolyvole.wheatandchessboard.game.Challenge
import dev.rolypolyvole.wheatandchessboard.util.Messages
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.LEVEL_GAMEMASTERS
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.hasPermission
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException

class SyncCommand {
    fun build(): LiteralArgumentBuilder<CommandSourceStack> {
        return literal("sync")
            .requires(hasPermission(LEVEL_GAMEMASTERS))
            .then(
                argument("time", greedyString())
                    .executes { sync(it.source, getString(it, "time")) }
            )
    }

    private fun sync(source: CommandSourceStack, time: String): Int {
        val challenge = Challenge.current ?: return 0
        val now = LocalDateTime.now()
        val target = parseTime(time.trim(), now)

        if (target == null) {
            source.sendFailure(Component.literal("Use a time like 00:00 or a date and time like 2026-11-12T00:00."))
            return 0
        }

        val timeLeft = Duration.between(now, target).toMillis()

        if (timeLeft <= 0) {
            source.sendFailure(Component.literal("That time is in the past."))
            return 0
        }

        if (!challenge.syncDayEnd(timeLeft)) {
            source.sendFailure(Component.literal("No challenge is running."))
            return 0
        }

        source.sendSuccess({ Component.literal("Day ${challenge.state.day} now ends in ${Messages.duration(timeLeft)}.") }, true)
        return Command.SINGLE_SUCCESS
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
}
