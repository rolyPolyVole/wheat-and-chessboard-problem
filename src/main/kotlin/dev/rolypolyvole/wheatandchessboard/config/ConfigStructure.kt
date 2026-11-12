package dev.rolypolyvole.wheatandchessboard.config

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item

@Suppress("UselessCallOnNotNull")
data class ChallengeConfig(
    val item: String = "minecraft:wheat",
    val function: String = "2^(t-1)",
    val dayInterval: Long = 86_400_000,
    val totalDays: Int = 14,
    val reminderIntervalSeconds: Int = 3600,
    val countdownSeconds: List<Int> = listOf(),
    val onDayComplete: Map<String, List<String>> = mapOf(),
    val onDayFail: Map<String, List<String>> = mapOf(),
    val onChallengeComplete: List<String> = listOf(),
    val messages: MessagesConfig? = MessagesConfig()
) {
    fun resolveItem(): Item? {
        val id = Identifier.tryParse(item.orEmpty()) ?: return null
        return BuiltInRegistries.ITEM.getOptional(id).orElse(null)
    }

    fun dayCompleteCommands(day: Int) = commandsFor(onDayComplete, day)

    fun dayFailCommands(day: Int) = commandsFor(onDayFail, day)

    private fun commandsFor(commands: Map<String, List<String>>?, day: Int): List<String> {
        val map = commands.orEmpty()
        return map[day.toString()] ?: map["default"] ?: listOf()
    }
}

data class MessagesConfig(
    val progressActionBar: String? = "<yellow><item>: <collected> / <total>",
    val progressChat: String? = null,
    val joinPending: String? = "<gold>Day <day> of <total_days>: <yellow>you need <remaining> more <item>. <time_left> left.",
    val joinComplete: String? = "<gold>Day <day> of <total_days>: <green>you have met the quota. <gray><time_left> until the next day.",
    val dayStart: String? = "<gold>Day <day> of <total_days> has begun! Collect <total> <item>.",
    val reminder: String? = "<red><time_left> left! You still need <remaining> more <item>.",
    val dayComplete: String? = "<green>You completed day <day>!",
    val dayFail: String? = "<red><player> failed day <day> and is out of the challenge.",
    val challengeEnd: String? = "<gold>The challenge is over! Survivors: <survivors>",
    val challengeEndNoSurvivors: String? = "<gold>The challenge is over! Nobody survived.",
    val info: String? = "<gold>Day <day> of <total_days>, <time_left> left. <players_left> of <players_total> players remain.",
    val infoProgress: String? = "<yellow><item>: <collected> / <total>",
    val infoEliminated: String? = "<red>You are out of the challenge."
)
