package dev.rolypolyvole.wheatandchessboard.config

import com.google.gson.Gson
import com.google.gson.JsonObject
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MessagesConfigTest {
    private val gson = Gson()
    private val defaults = gson.toJsonTree(MessagesConfig()).asJsonObject

    private val placeholders = listOf(
        "item", "day", "total_days", "total", "collected", "remaining",
        "time_left", "players_left", "players_total", "player", "survivors"
    )

    @Test
    fun defaultMessagesCloseEveryTag() {
        val strict = MiniMessage.builder().strict(true).build()
        val resolver = TagResolver.resolver(placeholders.map { Placeholder.unparsed(it, "x") })

        assertTrue(defaults.size() > 0)
        defaults.entrySet().forEach { strict.deserialize(it.value.asString, resolver) }
    }

    @Test
    fun defaultConfigFileMatchesTheBuiltInMessages() {
        val file = javaClass.getResource("/default_config.json")!!.readText()

        assertEquals(defaults, gson.fromJson(file, JsonObject::class.java).getAsJsonObject("messages"))
    }
}
