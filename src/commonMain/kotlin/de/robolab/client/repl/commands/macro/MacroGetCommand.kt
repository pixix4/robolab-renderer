package de.robolab.client.repl.commands.macro

import de.robolab.client.app.controller.MacroController
import de.robolab.client.repl.base.IReplExecutionContext
import de.robolab.client.repl.base.ReplBindableLeafCommand

object MacroGetCommand : ReplBindableLeafCommand<MacroController>(
    "get",
    "Show all commands for the given key binding",
    MacroController::class,
) {

    private val bindingParameter = MacroController.KeyBinding.param("binding")

    override suspend fun execute(binding: MacroController, context: IReplExecutionContext) {
        val keyBinding = context.getParameter(bindingParameter)

        val existing = binding.macroList.find {
            it.keyBinding == keyBinding
        }?.commands ?: emptyList()

        context.writeln(keyBinding.toString())
        for (c in existing) {
            context.write("  ")
            context.writeHighlightCommand(c)
            context.writeln()
        }
    }
}
