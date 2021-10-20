package de.robolab.client.repl.commands.macro

import de.robolab.client.app.controller.MacroController
import de.robolab.client.repl.base.IReplExecutionContext
import de.robolab.client.repl.base.ReplBindableLeafCommand

object MacroListCommand : ReplBindableLeafCommand<MacroController>(
    "list",
    "List all available macros",
    MacroController::class,
) {

    override suspend fun execute(binding: MacroController, context: IReplExecutionContext) {
        for (macro in binding.macroList) {
            context.writeln(macro.keyBinding.toString())
            for (c in macro.commands) {
                context.write("  ")
                context.writeHighlightCommand(c)
                context.writeln()
            }
        }
    }
}
