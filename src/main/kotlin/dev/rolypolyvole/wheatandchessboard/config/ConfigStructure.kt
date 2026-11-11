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
    val actionBar: Boolean = true,
    val messages: Boolean = false,
    val reminderIntervalSeconds: Int = 3600,
    val countdownSeconds: List<Int> = listOf(),
    val onDayComplete: Map<String, List<String>> = mapOf(),
    val onDayFail: Map<String, List<String>> = mapOf(),
    val onChallengeComplete: List<String> = listOf()
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
