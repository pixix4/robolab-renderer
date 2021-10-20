package de.robolab.client.repl.commands.macro

import de.robolab.client.app.controller.MacroController
import de.robolab.client.repl.base.IReplExecutionContext
import de.robolab.client.repl.base.ReplBindableLeafCommand

object MacroRestoreCommand : ReplBindableLeafCommand<MacroController>(
    "restore-defaults",
    "Delete all saved macros and restore the default bindings",
    MacroController::class,
) {

    override suspend fun execute(binding: MacroController, context: IReplExecutionContext) {
        binding.loadDefaults()
        binding.save()
    }
}
