package dev.rolypolyvole.wheatandchessboard.challenge

import dev.rolypolyvole.wheatandchessboard.config.ModConfig
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.ItemStack
import java.math.BigInteger

object Messages {
    private fun itemName(): Component {
        return ItemStack(ModConfig.item).itemName
    }

    private fun text(text: String, color: ChatFormatting): MutableComponent {
        return Component.literal(text).withStyle(color)
    }

    private fun withItem(before: String, after: String, color: ChatFormatting): MutableComponent {
        return Component.empty()
            .append(Component.literal(before))
            .append(itemName())
            .append(Component.literal(after))
            .withStyle(color)
    }

    fun progress(collected: BigInteger, quota: BigInteger): MutableComponent {
        val color = if (collected >= quota) ChatFormatting.GREEN else ChatFormatting.YELLOW
        return withItem("", ": $collected / $quota", color)
    }

    fun status(day: Int, totalDays: Int, collected: BigInteger, quota: BigInteger, timeLeft: Long): MutableComponent {
        val message = text("Day $day of $totalDays: ", ChatFormatting.GOLD)

        if (collected >= quota) {
            return message
                .append(text("you have met the quota. ", ChatFormatting.GREEN))
                .append(text("${duration(timeLeft)} until the next day.", ChatFormatting.GRAY))
        }

        return message.append(withItem("you need ${quota - collected} more ", ". ${duration(timeLeft)} left.", ChatFormatting.YELLOW))
    }

    fun dayStart(day: Int, totalDays: Int, quota: BigInteger): MutableComponent {
        return withItem("Day $day of $totalDays has begun! Collect $quota ", ".", ChatFormatting.GOLD)
    }

    fun reminder(timeLeft: Long, collected: BigInteger, quota: BigInteger): MutableComponent {
        return withItem("${duration(timeLeft)} left! You still need ${quota - collected} more ", ".", ChatFormatting.RED)
    }

    fun dayComplete(day: Int): MutableComponent {
        return text("You completed day $day!", ChatFormatting.GREEN)
    }

    fun dayFail(name: String, day: Int): MutableComponent {
        return text("$name failed day $day and is out of the challenge.", ChatFormatting.RED)
    }

    fun challengeEnd(survivors: List<String>): MutableComponent {
        val names = if (survivors.isEmpty()) "nobody" else survivors.joinToString(", ")
        return text("The challenge is over! Survivors: $names", ChatFormatting.GOLD)
    }

    fun duration(millis: Long): String {
        val seconds = (millis.coerceAtLeast(0) + 999) / 1000
        val hours = seconds / 3600
        val minutes = seconds % 3600 / 60
        val rest = seconds % 60

        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            minutes > 0 && rest > 0 -> "${minutes}m ${rest}s"
            minutes > 0 -> "${minutes}m"
            else -> "${rest}s"
        }
    }
}
