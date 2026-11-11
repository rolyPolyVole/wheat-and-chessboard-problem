package dev.rolypolyvole.wheatandchessboard.api

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

object ChallengeEvents {
    @JvmField
    val DAY_START: Event<DayStart> = EventFactory.createArrayBacked(DayStart::class.java) { listeners ->
        DayStart { server, day -> listeners.forEach { it.onDayStart(server, day) } }
    }

    @JvmField
    val DAY_COMPLETE: Event<DayComplete> = EventFactory.createArrayBacked(DayComplete::class.java) { listeners ->
        DayComplete { player, day -> listeners.forEach { it.onDayComplete(player, day) } }
    }

    @JvmField
    val DAY_FAIL: Event<DayFail> = EventFactory.createArrayBacked(DayFail::class.java) { listeners ->
        DayFail { server, player, day -> listeners.forEach { it.onDayFail(server, player, day) } }
    }

    @JvmField
    val CHALLENGE_END: Event<ChallengeEnd> = EventFactory.createArrayBacked(ChallengeEnd::class.java) { listeners ->
        ChallengeEnd { server, survivors -> listeners.forEach { it.onChallengeEnd(server, survivors) } }
    }

    fun interface DayStart {
        fun onDayStart(server: MinecraftServer, day: Int)
    }

    fun interface DayComplete {
        fun onDayComplete(player: ServerPlayer, day: Int)
    }

    fun interface DayFail {
        fun onDayFail(server: MinecraftServer, player: UUID, day: Int)
    }

    fun interface ChallengeEnd {
        fun onChallengeEnd(server: MinecraftServer, survivors: Set<UUID>)
    }
}
