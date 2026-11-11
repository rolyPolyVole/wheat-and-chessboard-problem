package dev.rolypolyvole.wheatandchessboard.challenge

import dev.rolypolyvole.wheatandchessboard.WheatAndChessboardMod
import net.minecraft.server.MinecraftServer

object Rewards {
    fun run(server: MinecraftServer, commands: List<String>?, player: String, day: Int) {
        val source = server.createCommandSourceStack().withSuppressedOutput()

        for (command in commands.orEmpty()) {
            val resolved = command
                .replace("{player}", player)
                .replace("{day}", day.toString())

            try {
                server.commands.performPrefixedCommand(source, resolved)
            } catch (e: Exception) {
                WheatAndChessboardMod.LOGGER.error("Failed to run command \"{}\"", resolved, e)
            }
        }
    }
}
