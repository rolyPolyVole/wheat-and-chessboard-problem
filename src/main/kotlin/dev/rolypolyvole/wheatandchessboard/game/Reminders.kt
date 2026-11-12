package dev.rolypolyvole.wheatandchessboard.game

import dev.rolypolyvole.wheatandchessboard.config.ChallengeConfig

@Suppress("UselessCallOnNotNull")
object Reminders {
    fun crossed(config: ChallengeConfig, before: Long, after: Long): Long? {
        if (after <= 0 || before <= after) return null

        val countdown = config.countdownSeconds.orEmpty()
            .map { it * 1000L }
            .filter { it in after until before }
            .minOrNull()

        if (countdown != null) return countdown

        val interval = config.reminderIntervalSeconds * 1000L
        if (interval <= 0 || (before - 1) / interval == (after - 1) / interval) return null

        return (before - 1) / interval * interval
    }
}
