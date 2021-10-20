package de.robolab.client.repl.commands.macro

import de.robolab.client.app.controller.MacroController
import de.robolab.client.repl.base.IReplExecutionContext
import de.robolab.client.repl.base.ReplBindableLeafCommand

object MacroExecuteCommand : ReplBindableLeafCommand<MacroController>(
    "execute",
    "Run a macro with the given key binding",
    MacroController::class,
) {

    private val bindingParameter = MacroController.KeyBinding.param("binding")

    override suspend fun execute(binding: MacroController, context: IReplExecutionContext) {
        val keyBinding = context.getParameter(bindingParameter)

        binding.execute(keyBinding, context)
    }
}
