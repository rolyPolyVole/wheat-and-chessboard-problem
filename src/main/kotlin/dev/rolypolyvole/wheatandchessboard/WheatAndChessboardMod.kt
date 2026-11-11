package dev.rolypolyvole.wheatandchessboard

import dev.rolypolyvole.wheatandchessboard.challenge.Challenge
import dev.rolypolyvole.wheatandchessboard.command.ChessboardCommand
import dev.rolypolyvole.wheatandchessboard.config.ModConfig
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class WheatAndChessboardMod : ModInitializer {

    override fun onInitialize() {
        ModConfig.load()
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register { _, _ -> ModConfig.load() }
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register { server, _, _ -> Challenge.collectAll(server) }

        ServerLifecycleEvents.SERVER_STARTED.register {
            ModConfig.load()
            Challenge.load(it)
        }
        ServerLifecycleEvents.SERVER_STOPPING.register { Challenge.save(it) }
        ServerTickEvents.END_SERVER_TICK.register { Challenge.tick(it) }
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ -> Challenge.onJoin(handler.player) }

        ChessboardCommand.register()

        LOGGER.info("Initialized Wheat and Chessboard Problem!")
    }

    companion object {
        const val MOD_ID = "wheat_and_chessboard"
        val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)
    }
}
