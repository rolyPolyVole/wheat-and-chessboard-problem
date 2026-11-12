package dev.rolypolyvole.wheatandchessboard.game

import dev.rolypolyvole.wheatandchessboard.api.ChallengeEvents
import dev.rolypolyvole.wheatandchessboard.config.ChallengeConfig
import dev.rolypolyvole.wheatandchessboard.config.MessagesConfig
import dev.rolypolyvole.wheatandchessboard.config.ModConfig
import dev.rolypolyvole.wheatandchessboard.storage.ChallengeState
import dev.rolypolyvole.wheatandchessboard.storage.PlayerProgress
import dev.rolypolyvole.wheatandchessboard.storage.StateStorage
import dev.rolypolyvole.wheatandchessboard.util.ItemCollector
import dev.rolypolyvole.wheatandchessboard.util.Messages
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import java.math.BigInteger
import java.util.UUID

class Challenge(private val server: MinecraftServer) {
    val state: ChallengeState = StateStorage.load(server)

    private val pendingScans = LinkedHashSet<UUID>()
    private var lastTickMillis = 0L
    private var ticks = 0
    private var dirty = false

    private val config: ChallengeConfig
        get() = ModConfig.config

    private val messages: MessagesConfig?
        get() = config.messages

    val quota: BigInteger
        get() = ModConfig.quota.forDay(state.day)

    val timeLeftMillis: Long
        get() = (config.dayInterval - state.elapsedMillis).coerceAtLeast(0)

    fun placeholders(progress: PlayerProgress? = null): Map<String, Any> {
        val quota = quota

        val values = mutableMapOf<String, Any>(
            "item" to Messages.itemName(ModConfig.item),
            "day" to state.day,
            "total_days" to config.totalDays,
            "total" to quota,
            "time_left" to Messages.duration(timeLeftMillis),
            "players_left" to state.players.values.count { !it.eliminated },
            "players_total" to state.players.size
        )

        if (progress != null) {
            values["player"] = progress.name
            values["collected"] = progress.collected
            values["remaining"] = (quota - progress.collected).max(BigInteger.ZERO)
        }

        return values
    }

    fun save() {
        StateStorage.save(server, state)
        dirty = false
    }

    fun start(day: Int): Boolean {
        if (state.active) return false

        state.active = true
        state.day = day
        state.elapsedMillis = 0

        state.players.values.forEach {
            it.completedDay = 0
            it.eliminated = false
        }

        beginDay()
        return true
    }

    fun end(): Boolean {
        if (!state.active) return false

        state.active = false
        save()
        return true
    }

    fun add(player: ServerPlayer): Boolean {
        val existing = state.players[player.uuid]
        if (existing != null && !existing.eliminated) return false

        if (existing != null) {
            existing.name = player.plainTextName
            existing.eliminated = false
        } else {
            state.players[player.uuid] = PlayerProgress(player.plainTextName)
        }

        collect(player)
        save()
        return true
    }

    fun remove(name: String): Boolean {
        val entry = state.players.entries.firstOrNull { it.value.name.equals(name, ignoreCase = true) }
        if (entry == null) return false

        state.players.remove(entry.key)
        save()
        return true
    }

    fun syncDayEnd(timeLeft: Long): Boolean {
        if (!state.active) return false

        state.elapsedMillis = config.dayInterval - timeLeft
        save()
        return true
    }

    fun isComplete(progress: PlayerProgress): Boolean {
        return progress.completedDay == state.day
    }

    fun onJoin(player: ServerPlayer) {
        val progress = state.players[player.uuid] ?: return
        progress.name = player.plainTextName

        if (!state.active || progress.eliminated) return

        collect(player)

        val template = if (isComplete(progress)) messages?.joinComplete else messages?.joinPending
        send(player, template, placeholders(progress))
    }

    fun onPickup(player: ServerPlayer, entity: ItemEntity) {
        val progress = pendingProgress(player) ?: return
        val stack = entity.item
        if (stack.item !== ModConfig.item) return

        val count = stack.count
        val amount = (quota - progress.collected).min(BigInteger.valueOf(count.toLong())).toInt()
        if (amount <= 0) return

        player.take(entity, amount)

        if (amount == count) {
            entity.discard()
        } else {
            entity.item = stack.copyWithCount(count - amount)
        }

        credit(player, progress, BigInteger.valueOf(amount.toLong()))
    }

    fun onInventoryChange(player: ServerPlayer, stack: ItemStack) {
        if (!state.active) return
        if (stack.item !== ModConfig.item) return

        pendingScans += player.uuid
    }

    fun collectAll() {
        server.playerList.players.forEach { collect(it) }
    }

    fun tick() {
        val now = System.nanoTime() / 1_000_000
        val delta = if (lastTickMillis == 0L) 0 else (now - lastTickMillis).coerceIn(0, MAX_TICK_MILLIS)
        lastTickMillis = now

        if (!state.active) return

        val before = timeLeftMillis
        state.elapsedMillis += delta
        sendReminders(before, timeLeftMillis)

        if (state.elapsedMillis >= config.dayInterval) {
            endDay()
            if (!state.active) return
        }

        collectPending()
        ticks++

        if (ticks % SAVE_INTERVAL_TICKS == 0 || (dirty && ticks % DIRTY_SAVE_INTERVAL_TICKS == 0)) save()
    }

    private fun collectPending() {
        if (pendingScans.isEmpty()) return

        val players = pendingScans.mapNotNull { server.playerList.getPlayer(it) }
        pendingScans.clear()
        players.forEach { collect(it) }
    }

    private fun pendingProgress(player: ServerPlayer): PlayerProgress? {
        if (!state.active) return null

        val progress = state.players[player.uuid] ?: return null
        if (progress.eliminated || isComplete(progress)) return null

        return progress
    }

    private fun collect(player: ServerPlayer) {
        val progress = pendingProgress(player) ?: return
        val taken = ItemCollector.take(player, ModConfig.item, quota - progress.collected)

        if (taken.signum() > 0) credit(player, progress, taken)
    }

    private fun credit(player: ServerPlayer, progress: PlayerProgress, amount: BigInteger) {
        progress.collected += amount
        dirty = true

        val values = placeholders(progress)

        Messages.format(messages?.progressActionBar, values)?.let { player.sendOverlayMessage(it) }
        send(player, messages?.progressChat, values)

        if (progress.collected >= quota) complete(player, progress)
    }

    private fun complete(player: ServerPlayer, progress: PlayerProgress) {
        val day = state.day
        progress.completedDay = day

        send(player, messages?.dayComplete, placeholders(progress))
        Rewards.run(server, config.dayCompleteCommands(day), progress.name, day)
        ChallengeEvents.DAY_COMPLETE.invoker().onDayComplete(player, day)
    }

    private fun beginDay() {
        val day = state.day
        val quota = quota

        state.players.values.forEach { it.collected = BigInteger.ZERO }

        if (quota.signum() == 0) {
            state.players.values.filter { !it.eliminated }.forEach { it.completedDay = day }
        }

        broadcast(messages?.dayStart, placeholders())
        ChallengeEvents.DAY_START.invoker().onDayStart(server, day)

        collectAll()
        save()
    }

    private fun endDay() {
        val config = config
        val day = state.day

        for ((uuid, progress) in state.players) {
            if (progress.eliminated || isComplete(progress)) continue

            progress.eliminated = true
            broadcast(messages?.dayFail, placeholders(progress))
            Rewards.run(server, config.dayFailCommands(day), progress.name, day)
            ChallengeEvents.DAY_FAIL.invoker().onDayFail(server, uuid, day)
        }

        state.elapsedMillis = (state.elapsedMillis - config.dayInterval).coerceIn(0, config.dayInterval - 1)

        if (day >= config.totalDays) {
            finish()
            return
        }

        state.day = day + 1
        beginDay()
    }

    private fun finish() {
        val survivors = state.players.filterValues { !it.eliminated }
        val template = if (survivors.isEmpty()) messages?.challengeEndNoSurvivors else messages?.challengeEnd

        state.active = false
        broadcast(template, placeholders() + ("survivors" to survivors.values.joinToString(", ") { it.name }))
        survivors.values.forEach { Rewards.run(server, config.onChallengeComplete, it.name, state.day) }
        ChallengeEvents.CHALLENGE_END.invoker().onChallengeEnd(server, survivors.keys.toSet())

        save()
    }

    private fun sendReminders(before: Long, after: Long) {
        val timeLeft = Reminders.crossed(config, before, after) ?: return

        for (player in server.playerList.players) {
            val progress = state.players[player.uuid] ?: continue
            if (progress.eliminated || isComplete(progress)) continue

            send(player, messages?.reminder, placeholders(progress) + ("time_left" to Messages.duration(timeLeft)))
        }
    }

    private fun send(player: ServerPlayer, template: String?, placeholders: Map<String, Any>) {
        Messages.format(template, placeholders)?.let { player.sendSystemMessage(it) }
    }

    private fun broadcast(template: String?, placeholders: Map<String, Any>) {
        Messages.format(template, placeholders)?.let { server.playerList.broadcastSystemMessage(it, false) }
    }

    companion object {
        private const val SAVE_INTERVAL_TICKS = 600
        private const val DIRTY_SAVE_INTERVAL_TICKS = 20
        private const val MAX_TICK_MILLIS = 1000L

        @JvmStatic
        var current: Challenge? = null
            private set

        fun open(server: MinecraftServer) {
            current = Challenge(server)
        }

        fun close() {
            current?.save()
            current = null
        }
    }
}
