package dev.rolypolyvole.wheatandchessboard.storage

import com.google.gson.JsonElement
import java.math.BigInteger
import java.util.UUID

data class ChallengeState(
    var active: Boolean = false,
    var day: Int = 0,
    var elapsedMillis: Long = 0,
    val players: MutableMap<UUID, PlayerProgress> = linkedMapOf()
)

data class PlayerProgress(
    var name: String = "",
    var collected: BigInteger = BigInteger.ZERO,
    var completedDay: Int = 0,
    var eliminated: Boolean = false,
    val pendingMessages: MutableList<JsonElement> = mutableListOf(),
    val pendingCommands: MutableList<String> = mutableListOf()
)
