package dev.rolypolyvole.wheatandchessboard.util

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import kotlin.test.Test
import kotlin.test.assertContains

class MessagesTest {
    private fun render(template: String, placeholders: Map<String, Any> = mapOf()): String {
        return GsonComponentSerializer.gson().serializeToTree(Messages.parse(template, placeholders)).toString()
    }

    @Test
    fun fillsPlaceholders() {
        val message = render("<gold>Day <day> of <total_days>", mapOf("day" to 3, "total_days" to 14))

        assertContains(message, "Day 3 of 14")
        assertContains(message, "\"color\":\"gold\"")
    }

    @Test
    fun keepsPlaceholderValuesLiteral() {
        assertContains(render("<player> failed", mapOf("player" to "<red>Steve")), "<red>Steve failed")
    }

    @Test
    fun writesClickAndHoverInTheCurrentFormat() {
        val message = render("<click:run_command:'/wheatandchess info'><hover:show_text:'More'>Info")

        assertContains(message, "\"click_event\":{\"action\":\"run_command\",\"command\":\"/wheatandchess info\"}")
        assertContains(message, "\"hover_event\":{\"action\":\"show_text\"")
    }
}
