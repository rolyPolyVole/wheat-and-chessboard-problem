package dev.rolypolyvole.wheatandchessboard.api

import dev.rolypolyvole.wheatandchessboard.challenge.Challenge
import dev.rolypolyvole.wheatandchessboard.config.ModConfig
import dev.rolypolyvole.wheatandchessboard.config.Quota
import java.math.BigInteger
import java.util.UUID

@Suppress("unused")
object WheatAndChessboardApi {
    @JvmStatic
    fun isActive(): Boolean = Challenge.state.active

    @JvmStatic
    fun currentDay(): Int = Challenge.state.day

    @JvmStatic
    fun totalDays(): Int = ModConfig.config.totalDays

    @JvmStatic
    fun quota(day: Int): BigInteger = Quota.forDay(day)

    @JvmStatic
    fun timeLeftMillis(): Long = Challenge.timeLeftMillis

    @JvmStatic
    fun participants(): Set<UUID> = Challenge.state.players.keys.toSet()

    @JvmStatic
    fun isParticipant(player: UUID): Boolean = player in Challenge.state.players

    @JvmStatic
    fun isEliminated(player: UUID): Boolean = Challenge.state.players[player]?.eliminated ?: false

    @JvmStatic
    fun collected(player: UUID): BigInteger = Challenge.state.players[player]?.collected ?: BigInteger.ZERO

    @JvmStatic
    fun hasCompletedToday(player: UUID): Boolean = Challenge.state.players[player]?.let { Challenge.isComplete(it) } ?: false
}
