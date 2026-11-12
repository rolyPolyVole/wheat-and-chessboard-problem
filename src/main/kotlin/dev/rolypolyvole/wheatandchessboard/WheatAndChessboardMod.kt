package dev.rolypolyvole.wheatandchessboard

import dev.rolypolyvole.wheatandchessboard.command.Commands
import dev.rolypolyvole.wheatandchessboard.config.ModConfig
import dev.rolypolyvole.wheatandchessboard.game.Challenge
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class WheatAndChessboardMod : ModInitializer {

    override fun onInitialize() {
        ModConfig.load()
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register { _, _ -> ModConfig.load() }
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register { _, _, _ -> Challenge.current?.collectAll() }

        ServerLifecycleEvents.SERVER_STARTED.register {
            ModConfig.load()
            Challenge.open(it)
        }

        ServerLifecycleEvents.SERVER_STOPPING.register { Challenge.close() }
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ -> Challenge.current?.onJoin(handler.player) }

        Commands.register()

        LOGGER.info("Initialized Wheat and Chessboard Problem!")
    }

    companion object {
        const val MOD_ID = "wheat_and_chessboard"
        val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)
    }
}
