package dev.rolypolyvole.wheatandchessboard.game

import dev.rolypolyvole.wheatandchessboard.config.ChallengeConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RemindersTest {
    private val config = ChallengeConfig(reminderIntervalSeconds = 3600, countdownSeconds = listOf(600, 60, 10))

    @Test
    fun firesWhenCrossingTheInterval() {
        assertEquals(7_200_000, Reminders.crossed(config, 7_200_020, 7_199_970))
    }

    @Test
    fun firesWhenCrossingACountdownStep() {
        assertEquals(600_000, Reminders.crossed(config, 600_010, 599_960))
        assertEquals(10_000, Reminders.crossed(config, 10_040, 9_990))
    }

    @Test
    fun staysQuietBetweenSteps() {
        assertNull(Reminders.crossed(config, 5_000_000, 4_999_950))
        assertNull(Reminders.crossed(config, 30_000, 29_950))
    }

    @Test
    fun staysQuietWhenTheDayIsOverOrTimeMovedBack() {
        assertNull(Reminders.crossed(config, 40, 0))
        assertNull(Reminders.crossed(config, 600_000, 700_000))
    }

    @Test
    fun canDisableTheInterval() {
        val quiet = ChallengeConfig(reminderIntervalSeconds = 0, countdownSeconds = listOf())
        assertNull(Reminders.crossed(quiet, 7_200_020, 7_199_970))
    }
}
