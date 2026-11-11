package dev.rolypolyvole.wheatandchessboard.challenge

import dev.rolypolyvole.wheatandchessboard.api.ChallengeEvents
import dev.rolypolyvole.wheatandchessboard.config.ModConfig
import dev.rolypolyvole.wheatandchessboard.config.Quota
import net.minecraft.core.component.DataComponents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import java.math.BigInteger
import java.util.UUID

@Suppress("UselessCallOnNotNull")
object Challenge {
    private const val SAVE_INTERVAL_TICKS = 600
    private const val DIRTY_SAVE_INTERVAL_TICKS = 20
    private const val MAX_TICK_MILLIS = 1000L

    var state = ChallengeState()
        private set

    private val pendingScans = LinkedHashSet<UUID>()
    private var lastTickMillis = 0L
    private var ticks = 0
    private var dirty = false

    val quota: BigInteger
        get() = Quota.forDay(state.day)

    val timeLeftMillis: Long
        get() = (ModConfig.config.dayInterval - state.elapsedMillis).coerceAtLeast(0)

    fun load(server: MinecraftServer) {
        state = StateStorage.load(server)
        pendingScans.clear()
        lastTickMillis = 0
        dirty = false
    }

    fun save(server: MinecraftServer) {
        StateStorage.save(server, state)
        dirty = false
    }

    fun start(server: MinecraftServer, day: Int): Boolean {
        if (state.active) return false

        state.active = true
        state.day = day
        state.elapsedMillis = 0

        state.players.values.forEach {
            it.completedDay = 0
            it.eliminated = false
        }

        beginDay(server)
        return true
    }

    fun end(server: MinecraftServer): Boolean {
        if (!state.active) return false

        state.active = false
        save(server)
        return true
    }

    fun add(server: MinecraftServer, player: ServerPlayer): Boolean {
        val existing = state.players[player.uuid]
        if (existing != null && !existing.eliminated) return false

        if (existing != null) {
            existing.name = player.plainTextName
            existing.eliminated = false
        } else {
            state.players[player.uuid] = PlayerProgress(player.plainTextName)
        }

        collect(player)
        save(server)
        return true
    }

    fun remove(server: MinecraftServer, name: String): Boolean {
        val entry = state.players.entries.firstOrNull { it.value.name.equals(name, ignoreCase = true) }
        if (entry == null) return false

        state.players.remove(entry.key)
        save(server)
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
        player.sendSystemMessage(Messages.status(state.day, ModConfig.config.totalDays, progress.collected, quota, timeLeftMillis))
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
        if (stack.item !== ModConfig.item && !stack.has(DataComponents.CONTAINER)) return

        pendingScans += player.uuid
    }

    fun syncDayEnd(server: MinecraftServer, timeLeft: Long): Boolean {
        if (!state.active) return false

        state.elapsedMillis = ModConfig.config.dayInterval - timeLeft
        save(server)
        return true
    }

    fun collectAll(server: MinecraftServer) {
        server.playerList.players.forEach { collect(it) }
    }

    fun tick(server: MinecraftServer) {
        val now = System.nanoTime() / 1_000_000
        val delta = if (lastTickMillis == 0L) 0 else (now - lastTickMillis).coerceIn(0, MAX_TICK_MILLIS)
        lastTickMillis = now

        if (!state.active) return

        val before = timeLeftMillis
        state.elapsedMillis += delta
        val after = timeLeftMillis

        sendReminders(server, before, after)

        if (state.elapsedMillis >= ModConfig.config.dayInterval) {
            endDay(server)
            if (!state.active) return
        }

        if (pendingScans.isNotEmpty()) {
            val players = pendingScans.mapNotNull { server.playerList.getPlayer(it) }
            pendingScans.clear()
            players.forEach { collect(it) }
        }

        ticks++

        if (ticks % SAVE_INTERVAL_TICKS == 0 || (dirty && ticks % DIRTY_SAVE_INTERVAL_TICKS == 0)) save(server)
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
        val config = ModConfig.config
        val quota = quota

        progress.collected += amount
        dirty = true

        if (config.actionBar) player.sendOverlayMessage(Messages.progress(progress.collected, quota))
        if (config.messages) player.sendSystemMessage(Messages.progress(progress.collected, quota))

        if (progress.collected >= quota) complete(player, progress)
    }

    private fun complete(player: ServerPlayer, progress: PlayerProgress) {
        val day = state.day
        progress.completedDay = day

        player.sendSystemMessage(Messages.dayComplete(day))
        Rewards.run(player.level().server, ModConfig.config.dayCompleteCommands(day), progress.name, day)
        ChallengeEvents.DAY_COMPLETE.invoker().onDayComplete(player, day)
    }

    private fun beginDay(server: MinecraftServer) {
        val day = state.day
        val quota = quota

        state.players.values.forEach { it.collected = BigInteger.ZERO }

        if (quota.signum() == 0) {
            state.players.values.filter { !it.eliminated }.forEach { it.completedDay = day }
        }

        server.playerList.broadcastSystemMessage(Messages.dayStart(day, ModConfig.config.totalDays, quota), false)
        ChallengeEvents.DAY_START.invoker().onDayStart(server, day)

        collectAll(server)
        save(server)
    }

    private fun endDay(server: MinecraftServer) {
        val config = ModConfig.config
        val day = state.day

        for ((uuid, progress) in state.players) {
            if (progress.eliminated || isComplete(progress)) continue

            progress.eliminated = true
            server.playerList.broadcastSystemMessage(Messages.dayFail(progress.name, day), false)
            Rewards.run(server, config.dayFailCommands(day), progress.name, day)
            ChallengeEvents.DAY_FAIL.invoker().onDayFail(server, uuid, day)
        }

        state.elapsedMillis = (state.elapsedMillis - config.dayInterval).coerceIn(0, config.dayInterval - 1)

        if (day >= config.totalDays) {
            finish(server)
            return
        }

        state.day = day + 1
        beginDay(server)
    }

    private fun finish(server: MinecraftServer) {
        val survivors = state.players.filterValues { !it.eliminated }

        state.active = false
        server.playerList.broadcastSystemMessage(Messages.challengeEnd(survivors.values.map { it.name }), false)
        survivors.values.forEach { Rewards.run(server, ModConfig.config.onChallengeComplete, it.name, state.day) }
        ChallengeEvents.CHALLENGE_END.invoker().onChallengeEnd(server, survivors.keys.toSet())

        save(server)
    }

    private fun sendReminders(server: MinecraftServer, before: Long, after: Long) {
        val timeLeft = reminderCrossed(before, after) ?: return
        val quota = quota

        for (player in server.playerList.players) {
            val progress = state.players[player.uuid] ?: continue
            if (progress.eliminated || isComplete(progress)) continue

            player.sendSystemMessage(Messages.reminder(timeLeft, progress.collected, quota))
        }
    }

    private fun reminderCrossed(before: Long, after: Long): Long? {
        if (after <= 0 || before == after) return null

        val config = ModConfig.config

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
