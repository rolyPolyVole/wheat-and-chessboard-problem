package dev.rolypolyvole.wheatandchessboard.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import dev.rolypolyvole.wheatandchessboard.game.Challenge
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.commands.Commands as VanillaCommands

typealias CommandBuilder = LiteralArgumentBuilder<CommandSourceStack>
typealias Context = CommandContext<CommandSourceStack>

private val ADMIN = VanillaCommands.hasPermission<CommandSourceStack>(VanillaCommands.LEVEL_GAMEMASTERS)

fun command(name: String, build: CommandBuilder.() -> Unit): CommandBuilder {
    return VanillaCommands.literal(name).apply(build)
}

fun <T : Any> ArgumentBuilder<CommandSourceStack, *>.argument(
    name: String,
    type: ArgumentType<T>,
    build: RequiredArgumentBuilder<CommandSourceStack, T>.() -> Unit
) {
    then(VanillaCommands.argument(name, type).apply(build))
}

fun ArgumentBuilder<CommandSourceStack, *>.adminOnly() {
    requires(ADMIN)
}

fun ArgumentBuilder<CommandSourceStack, *>.runs(action: Context.() -> Unit) {
    executes {
        it.action()
        Command.SINGLE_SUCCESS
    }
}

val Context.challenge: Challenge
    get() = Challenge.current ?: fail("The challenge is not available.")

fun Context.reply(message: String) {
    source.sendSuccess({ Component.literal(message) }, true)
}

fun Context.send(message: Component) {
    source.sendSuccess({ message }, false)
}

fun fail(message: String): Nothing {
    throw SimpleCommandExceptionType(Component.literal(message)).create()
}
