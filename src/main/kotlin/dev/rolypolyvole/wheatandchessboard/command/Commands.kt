package dev.rolypolyvole.wheatandchessboard.command

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback

object Commands {
    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(command("wheatandchess") {
                then(startCommand())
                then(endCommand())
                then(syncCommand())
                then(addCommand())
                then(removeCommand())
                then(infoCommand())
            })
        }
    }
}
