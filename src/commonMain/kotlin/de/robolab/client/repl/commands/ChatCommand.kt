package de.robolab.client.repl.commands

import de.robolab.client.repl.base.IReplExecutionContext
import de.robolab.client.repl.base.ReplBindableLeafCommand
import de.robolab.client.repl.base.ReplColor

object ChatCommand: ReplBindableLeafCommand<Unit>(
    "chat",
    "Start an interactive chat client",
    Unit::class
) {

    override suspend fun execute(binding: Unit, context: IReplExecutionContext) {
        context.writeln("Please enter your message:")
        while (true) {
            context.write("> ")
            val line = context.readInputLine()
            context.writeln()
            context.write("Your message was: ")
            context.writeln(line, ReplColor.BLUE)
        }
    }
}
