package dev.rolypolyvole.wheatandchessboard

import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class WheatAndChessboardMod : ModInitializer {

    override fun onInitialize() {
        LOGGER.info("Initialized Wheat and Chessboard Problem!")
    }

    companion object {
        const val MOD_ID = "wheat_and_chessboard"
        val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)
    }
}
