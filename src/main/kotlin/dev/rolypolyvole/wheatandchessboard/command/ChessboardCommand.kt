package dev.rolypolyvole.wheatandchessboard.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import dev.rolypolyvole.wheatandchessboard.challenge.Challenge
import dev.rolypolyvole.wheatandchessboard.challenge.Messages
import dev.rolypolyvole.wheatandchessboard.config.ModConfig
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException

object ChessboardCommand {
    private val ADMIN = Commands.hasPermission<CommandSourceStack>(Commands.LEVEL_GAMEMASTERS)

    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                Commands.literal("wheatandchess")
                    .then(Commands.literal("start").requires(ADMIN)
                        .executes { start(it.source, 1) }
                        .then(Commands.argument("day", IntegerArgumentType.integer(1))
                            .executes { start(it.source, IntegerArgumentType.getInteger(it, "day")) }
                        )
                    )
                    .then(Commands.literal("end").requires(ADMIN)
                        .executes { end(it.source) }
                    )
                    .then(Commands.literal("sync").requires(ADMIN)
                        .then(Commands.argument("time", StringArgumentType.greedyString())
                            .executes { sync(it.source, StringArgumentType.getString(it, "time")) }
                        )
                    )
                    .then(Commands.literal("add").requires(ADMIN)
                        .then(Commands.literal("all")
                            .executes { add(it.source, it.source.server.playerList.players) }
                        )
                        .then(Commands.argument("players", EntityArgument.players())
                            .executes { add(it.source, EntityArgument.getPlayers(it, "players")) }
                        )
                    )
                    .then(Commands.literal("remove").requires(ADMIN)
                        .then(Commands.argument("player", StringArgumentType.word())
                            .suggests { _, builder -> SharedSuggestionProvider.suggest(Challenge.state.players.values.map { it.name }, builder) }
                            .executes { remove(it.source, StringArgumentType.getString(it, "player")) }
                        )
                    )
                    .then(Commands.literal("info")
                        .executes { info(it.source) }
                    )
            )
        }
    }

    private fun start(source: CommandSourceStack, day: Int): Int {
        if (day > ModConfig.config.totalDays) {
            return fail(source, "The challenge only has ${ModConfig.config.totalDays} days.")
        }

        if (!Challenge.start(source.server, day)) {
            return fail(source, "The challenge is already running.")
        }

        val players = Challenge.state.players.size
        val hint = if (players == 0) " Nobody is in it yet, add players with /wheatandchess add." else " $players player(s) are in it."

        return success(source, "Started the challenge on day $day.$hint")
    }

    private fun end(source: CommandSourceStack): Int {
        if (!Challenge.end(source.server)) return fail(source, "No challenge is running.")

        return success(source, "Ended the challenge.")
    }

    private fun sync(source: CommandSourceStack, time: String): Int {
        val now = LocalDateTime.now()
        val target = parseTime(time.trim(), now) ?: return fail(source, "Use a time like 00:00 or a date and time like 2026-11-12T00:00.")
        val timeLeft = Duration.between(now, target).toMillis()

        if (timeLeft <= 0) return fail(source, "That time is in the past.")
        if (!Challenge.syncDayEnd(source.server, timeLeft)) return fail(source, "No challenge is running.")

        return success(source, "Day ${Challenge.state.day} now ends in ${Messages.duration(timeLeft)}.")
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

    private fun add(source: CommandSourceStack, players: Collection<ServerPlayer>): Int {
        val added = players.count { Challenge.add(source.server, it) }
        if (added == 0) return fail(source, "Those players are already in the challenge.")

        return success(source, "Added $added player(s) to the challenge.")
    }

    private fun remove(source: CommandSourceStack, name: String): Int {
        if (!Challenge.remove(source.server, name)) return fail(source, "$name is not in the challenge.")

        return success(source, "Removed $name from the challenge.")
    }

    private fun info(source: CommandSourceStack): Int {
        val state = Challenge.state
        if (!state.active) return fail(source, "No challenge is running.")

        val config = ModConfig.config
        val remaining = state.players.values.count { !it.eliminated }
        val progress = source.player?.let { state.players[it.uuid] }

        source.sendSuccess({
            Component.literal("Day ${state.day} of ${config.totalDays}, ${Messages.duration(Challenge.timeLeftMillis)} left. ")
                .append(Component.literal("$remaining of ${state.players.size} players remain."))
                .withStyle(ChatFormatting.GOLD)
        }, false)

        if (progress == null) return 1

        if (progress.eliminated) {
            source.sendSuccess({ Component.literal("You are out of the challenge.").withStyle(ChatFormatting.RED) }, false)
        } else {
            source.sendSuccess({ Messages.progress(progress.collected, Challenge.quota) }, false)
        }

        return 1
    }

    private fun success(source: CommandSourceStack, message: String): Int {
        source.sendSuccess({ Component.literal(message) }, true)
        return 1
    }

    private fun fail(source: CommandSourceStack, message: String): Int {
        source.sendFailure(Component.literal(message))
        return 0
    }
}
