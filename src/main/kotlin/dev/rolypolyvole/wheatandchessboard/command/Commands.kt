package dev.rolypolyvole.wheatandchessboard.command

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.Commands.literal

object Commands {
    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                literal("wheatandchess")
                    .then(StartCommand().build())
                    .then(EndCommand().build())
                    .then(SyncCommand().build())
                    .then(AddCommand().build())
                    .then(RemoveCommand().build())
                    .then(InfoCommand().build())
            )
        }
    }
}
