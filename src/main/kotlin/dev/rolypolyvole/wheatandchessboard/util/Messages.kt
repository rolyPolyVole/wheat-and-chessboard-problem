package dev.rolypolyvole.wheatandchessboard.util

import com.mojang.serialization.JsonOps
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.world.item.Item
import net.kyori.adventure.text.Component as AdventureComponent

object Messages {
    private val miniMessage = MiniMessage.miniMessage()
    private val json = GsonComponentSerializer.gson()

    fun format(template: String?, placeholders: Map<String, Any>): Component? {
        if (template == null) return null

        val tree = json.serializeToTree(parse(template, placeholders))
        return ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, tree).getOrThrow()
    }

    fun parse(template: String, placeholders: Map<String, Any>): AdventureComponent {
        val resolvers = placeholders.map { (name, value) ->
            if (value is AdventureComponent) Placeholder.component(name, value) else Placeholder.unparsed(name, value.toString())
        }

        return miniMessage.deserialize(template, TagResolver.resolver(resolvers))
    }

    fun itemName(item: Item): AdventureComponent {
        return AdventureComponent.translatable(item.descriptionId)
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
