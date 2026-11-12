package dev.rolypolyvole.wheatandchessboard.command

internal fun endCommand() = command("end") {
    adminOnly()
    runs { end() }
}

private fun Context.end() {
    if (!challenge.end()) fail("No challenge is running.")
    reply("Ended the challenge.")
}
