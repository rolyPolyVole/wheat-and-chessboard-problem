package dev.rolypolyvole.wheatandchessboard.util

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import java.math.BigInteger

object ItemCollector {
    private val MAX = BigInteger.valueOf(Long.MAX_VALUE)

    fun take(player: ServerPlayer, item: Item, limit: BigInteger): BigInteger {
        val inventory = player.inventory
        var remaining = limit.min(MAX).toLong()
        var total = 0L

        for (slot in 0 until inventory.containerSize) {
            if (remaining <= 0) break

            val stack = inventory.getItem(slot)
            if (stack.item !== item) continue

            val amount = minOf(stack.count.toLong(), remaining).toInt()
            stack.shrink(amount)

            remaining -= amount
            total += amount
        }

        if (total > 0) inventory.setChanged()
        return BigInteger.valueOf(total)
    }
}
