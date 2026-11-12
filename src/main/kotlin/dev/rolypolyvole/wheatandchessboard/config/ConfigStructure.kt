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
    val progressActionBar: String? = "<gray>Items collected: <aqua><collected>/<total></gray>",
    val dayStart: String? = "<gray><b>Day <day></b> has begun! Collect <gold><total>x <item></gold>.</gray>",
    val reminder: String? = "<gray><b><red><time_left> left!</red></b> You still need <aqua><remaining></aqua>x more <gold><item></gold>.",
    val dayComplete: String? = "<green>You completed <b>day <day></b>, congrats!",
    val dayFail: String? = "<red><gray><player></gray> failed <b>day <day></b> and is eliminated.",
    val challengeEnd: String? = "<gold><b>The challenge is over!</b> There were <aqua><survivors></aqua> survivors.",
    val info: String? = "<gray>Info on: <b>Day <day></b><newline><green><players_left></green>/<yellow><players_total></yellow> players left<newline><red><b><time_left></b></red> remain",
    val infoProgress: String? = "<gray>Items collected: <aqua><collected>/<total></gray>",
    val infoEliminated: String? = "<red>You are out of the challenge."
)
