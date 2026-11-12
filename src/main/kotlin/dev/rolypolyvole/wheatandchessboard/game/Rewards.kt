package dev.rolypolyvole.wheatandchessboard.game

import dev.rolypolyvole.wheatandchessboard.WheatAndChessboardMod
import net.minecraft.server.MinecraftServer

object Rewards {
    fun resolve(commands: List<String>?, player: String, day: Int): List<String> {
        return commands.orEmpty().map {
            it.replace("{player}", player).replace("{day}", day.toString())
        }
    }

    fun run(server: MinecraftServer, commands: List<String>) {
        val source = server.createCommandSourceStack().withSuppressedOutput()

        for (command in commands) {
            try {
                server.commands.performPrefixedCommand(source, command)
            } catch (e: Exception) {
                WheatAndChessboardMod.LOGGER.error("Failed to run command \"{}\"", command, e)
            }
        }
    }
}
