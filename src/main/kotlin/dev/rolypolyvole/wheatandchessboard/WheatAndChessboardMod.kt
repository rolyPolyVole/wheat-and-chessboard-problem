package dev.rolypolyvole.wheatandchessboard

import dev.rolypolyvole.wheatandchessboard.config.ModConfig
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class WheatAndChessboardMod : ModInitializer {

    override fun onInitialize() {
        ModConfig.load()
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register { _, _ -> ModConfig.load() }

        LOGGER.info("Initialized Wheat and Chessboard Problem!")
    }

    companion object {
        const val MOD_ID = "wheat_and_chessboard"
        val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)
    }
}
