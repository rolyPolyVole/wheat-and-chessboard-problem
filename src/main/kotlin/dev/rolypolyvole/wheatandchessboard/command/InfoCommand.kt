package dev.rolypolyvole.wheatandchessboard.command

import dev.rolypolyvole.wheatandchessboard.config.ModConfig
import dev.rolypolyvole.wheatandchessboard.util.Messages
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

internal fun infoCommand() = command("info") {
    runs { info() }
}

private fun Context.info() {
    val challenge = challenge
    val state = challenge.state

    if (!state.active) fail("No challenge is running.")

    val remaining = state.players.values.count { !it.eliminated }
    val timeLeft = Messages.duration(challenge.timeLeftMillis)

    send(
        Component.literal("Day ${state.day} of ${ModConfig.config.totalDays}, $timeLeft left. ")
            .append(Component.literal("$remaining of ${state.players.size} players remain."))
            .withStyle(ChatFormatting.GOLD)
    )

    val progress = source.player?.let { state.players[it.uuid] } ?: return

    if (progress.eliminated) {
        send(Component.literal("You are out of the challenge.").withStyle(ChatFormatting.RED))
    } else {
        send(Messages.progress(progress.collected, challenge.quota))
    }
}
