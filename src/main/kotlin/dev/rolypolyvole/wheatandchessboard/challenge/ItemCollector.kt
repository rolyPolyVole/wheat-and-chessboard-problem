package dev.rolypolyvole.wheatandchessboard.challenge

import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.math.BigInteger

object ItemCollector {
    private val MAX = BigInteger.valueOf(Long.MAX_VALUE)

    fun take(player: ServerPlayer, item: Item, limit: BigInteger): BigInteger {
        var remaining = limit.min(MAX).toLong()
        var total = 0L

        for (container in listOf(player.inventory, player.enderChestInventory)) {
            var changed = false

            for (slot in 0 until container.containerSize) {
                if (remaining <= 0) break

                val taken = takeFrom(container.getItem(slot), item, remaining)
                if (taken == 0L) continue

                remaining -= taken
                total += taken
                changed = true
            }

            if (changed) container.setChanged()
        }

        return BigInteger.valueOf(total)
    }

    private fun takeFrom(stack: ItemStack, item: Item, limit: Long): Long {
        if (stack.isEmpty || limit <= 0) return 0

        if (stack.item === item) {
            val amount = minOf(stack.count.toLong(), limit).toInt()
            stack.shrink(amount)
            return amount.toLong()
        }

        if (stack.count != 1) return 0

        val container = stack.get(DataComponents.CONTAINER)

        if (container != null) {
            val items = container.itemCopies().toList()
            val taken = takeFromAll(items, item, limit)

            if (taken > 0) stack.set(DataComponents.CONTAINER, container.copyWithContents(items.stream()))
            return taken
        }

        val bundle = stack.get(DataComponents.BUNDLE_CONTENTS)

        if (bundle != null) {
            val items = bundle.itemCopies().toList()
            val taken = takeFromAll(items, item, limit)

            if (taken > 0) stack.set(DataComponents.BUNDLE_CONTENTS, bundle.copyWithContents(items.stream().filter { !it.isEmpty }))
            return taken
        }

        return 0
    }

    private fun takeFromAll(items: List<ItemStack>, item: Item, limit: Long): Long {
        var taken = 0L

        for (stack in items) {
            if (taken >= limit) break
            taken += takeFrom(stack, item, limit - taken)
        }

        return taken
    }
}
