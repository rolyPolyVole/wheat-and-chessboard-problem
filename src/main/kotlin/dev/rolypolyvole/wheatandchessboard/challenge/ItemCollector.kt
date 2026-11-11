package dev.rolypolyvole.wheatandchessboard.challenge

import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.math.BigInteger

object ItemCollector {
    private val MAX = BigInteger.valueOf(Long.MAX_VALUE)

    fun take(player: ServerPlayer, item: Item, limit: BigInteger): BigInteger {
        val inventory = player.inventory
        var remaining = limit.min(MAX).toLong()
        var total = 0L

        for (slot in 0 until inventory.containerSize) {
            if (remaining <= 0) break

            val taken = takeFrom(inventory.getItem(slot), item, remaining)

            remaining -= taken
            total += taken
        }

        if (total > 0) inventory.setChanged()
        return BigInteger.valueOf(total)
    }

    private fun takeFrom(stack: ItemStack, item: Item, limit: Long): Long {
        if (stack.isEmpty) return 0

        if (stack.item === item) {
            val amount = minOf(stack.count.toLong(), limit).toInt()
            stack.shrink(amount)
            return amount.toLong()
        }

        if (stack.count != 1) return 0

        val container = stack.get(DataComponents.CONTAINER) ?: return 0
        val items = container.itemCopies().toList()
        var taken = 0L

        for (inner in items) {
            if (taken >= limit) break
            if (inner.item !== item) continue

            val amount = minOf(inner.count.toLong(), limit - taken).toInt()
            inner.shrink(amount)
            taken += amount
        }

        if (taken > 0) stack.set(DataComponents.CONTAINER, container.copyWithContents(items.stream()))
        return taken
    }
}
