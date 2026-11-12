package dev.rolypolyvole.wheatandchessboard.storage

import com.google.gson.FormattingStyle
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import dev.rolypolyvole.wheatandchessboard.WheatAndChessboardMod
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.LevelResource
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object StateStorage {
    private val gson = GsonBuilder()
        .setFormattingStyle(FormattingStyle.PRETTY.withIndent(" ".repeat(4)))
        .create()

    private fun path(server: MinecraftServer): Path {
        return server.getWorldPath(LevelResource.ROOT).resolve("${WheatAndChessboardMod.MOD_ID}.json")
    }

    fun load(server: MinecraftServer): ChallengeState {
        val path = path(server)
        if (!path.exists()) return ChallengeState()

        return try {
            gson.fromJson(path.readText(), ChallengeState::class.java) ?: ChallengeState()
        } catch (e: JsonParseException) {
            WheatAndChessboardMod.LOGGER.error("Failed to read challenge data {}, starting with no challenge", path, e)
            ChallengeState()
        }
    }

    fun save(server: MinecraftServer, state: ChallengeState) {
        val path = path(server)
        val temp = path.resolveSibling("${path.fileName}.tmp")

        try {
            temp.writeText(gson.toJson(state))
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: IOException) {
            WheatAndChessboardMod.LOGGER.error("Failed to save challenge data {}", path, e)
        }
    }
}
