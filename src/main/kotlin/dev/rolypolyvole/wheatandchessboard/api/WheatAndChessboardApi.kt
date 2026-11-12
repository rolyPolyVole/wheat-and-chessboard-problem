package dev.rolypolyvole.wheatandchessboard.api

import dev.rolypolyvole.wheatandchessboard.config.ModConfig
import dev.rolypolyvole.wheatandchessboard.game.Challenge
import dev.rolypolyvole.wheatandchessboard.storage.PlayerProgress
import java.math.BigInteger
import java.util.UUID

@Suppress("unused")
object WheatAndChessboardApi {
    private val challenge: Challenge?
        get() = Challenge.current

    private fun progress(player: UUID): PlayerProgress? = challenge?.state?.players?.get(player)

    @JvmStatic
    fun isActive(): Boolean = challenge?.state?.active ?: false

    @JvmStatic
    fun currentDay(): Int = challenge?.state?.day ?: 0

    @JvmStatic
    fun totalDays(): Int = ModConfig.config.totalDays

    @JvmStatic
    fun quota(day: Int): BigInteger = ModConfig.quota.forDay(day)

    @JvmStatic
    fun timeLeftMillis(): Long = challenge?.timeLeftMillis ?: 0

    @JvmStatic
    fun participants(): Set<UUID> = challenge?.state?.players?.keys?.toSet() ?: setOf()

    @JvmStatic
    fun isParticipant(player: UUID): Boolean = progress(player) != null

    @JvmStatic
    fun isEliminated(player: UUID): Boolean = progress(player)?.eliminated ?: false

    @JvmStatic
    fun collected(player: UUID): BigInteger = progress(player)?.collected ?: BigInteger.ZERO

    @JvmStatic
    fun hasCompletedToday(player: UUID): Boolean = progress(player)?.let { challenge?.isComplete(it) } ?: false
}
