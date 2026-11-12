package dev.rolypolyvole.wheatandchessboard.config

import com.google.gson.FormattingStyle
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import dev.rolypolyvole.wheatandchessboard.WheatAndChessboardMod
import dev.rolypolyvole.wheatandchessboard.util.Messages
import dev.rolypolyvole.wheatandchessboard.util.Quota
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import java.nio.file.Files
import kotlin.io.path.exists
import kotlin.io.path.readText

@Suppress("UselessCallOnNotNull")
object ModConfig {
    private val gson = GsonBuilder()
        .setFormattingStyle(FormattingStyle.PRETTY.withIndent(" ".repeat(4)))
        .serializeNulls()
        .create()

    private val path = FabricLoader.getInstance()
        .configDir
        .resolve("${WheatAndChessboardMod.MOD_ID}.json")

    private val resourcePath = FabricLoader.getInstance()
        .getModContainer(WheatAndChessboardMod.MOD_ID)
        .flatMap { it.findPath("default_config.json") }
        .orElseThrow()

    var config = defaultConfig()
        private set

    var item: Item = Items.WHEAT
        private set

    var quota = Quota(config.function)
        private set

    fun load() {
        val loaded = try {
            if (path.exists()) {
                gson.fromJson(path.readText(), ChallengeConfig::class.java) ?: defaultConfig()
            } else {
                Files.copy(resourcePath, path)
                defaultConfig()
            }
        } catch (e: JsonParseException) {
            WheatAndChessboardMod.LOGGER.error("Failed to read config {}, keeping the previous config", path, e)
            return
        }

        val problems = validate(loaded)

        if (problems.isNotEmpty()) {
            WheatAndChessboardMod.LOGGER.error("Invalid config {}, keeping the previous config:\n{}", path, problems.joinToString("\n"))
            return
        }

        config = loaded
        item = loaded.resolveItem() ?: Items.WHEAT
        quota = Quota(loaded.function)
    }

    private fun defaultConfig(): ChallengeConfig {
        return gson.fromJson(resourcePath.readText(), ChallengeConfig::class.java)
    }

    private fun validate(config: ChallengeConfig): List<String> {
        val problems = mutableListOf<String>()

        if (config.resolveItem() == null) problems += "Unknown item: ${config.item}"
        if (config.dayInterval < 1000) problems += "dayInterval has to be at least 1000 milliseconds"
        if (config.totalDays < 1) problems += "totalDays cannot be less than 1"
        if (config.reminderIntervalSeconds < 0) problems += "reminderIntervalSeconds cannot be negative"
        if (config.countdownSeconds.orEmpty().any { it < 1 }) problems += "countdownSeconds cannot contain values less than 1"

        try {
            Quota.evaluate(config.function.orEmpty(), 1)
        } catch (e: Exception) {
            problems += "Invalid function \"${config.function}\": ${e.message}"
        }

        for ((name, template) in messageTemplates(config)) {
            try {
                Messages.format(template, mapOf())
            } catch (e: Exception) {
                problems += "Invalid message \"$name\": ${e.message}"
            }
        }

        return problems
    }

    private fun messageTemplates(config: ChallengeConfig): Map<String, String> {
        val messages = config.messages ?: return mapOf()

        return gson.toJsonTree(messages).asJsonObject.entrySet()
            .filter { !it.value.isJsonNull }
            .associate { it.key to it.value.asString }
    }
}
