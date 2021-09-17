package de.robolab.client.repl.commands.macro

import de.robolab.client.app.controller.MacroController
import de.robolab.client.repl.base.IReplExecutionContext
import de.robolab.client.repl.base.ReplBindableLeafCommand
import de.robolab.client.repl.base.StringParameter

object MacroAddCommand : ReplBindableLeafCommand<MacroController>(
    "add",
    "Add a new command. If a macro with the given key binding already exists, the given command will be added to the existing macro",
    MacroController::class,
) {

    private val bindingParameter = MacroController.KeyBinding.param("binding")
    private val commandParameter = StringParameter.param("command")

    override suspend fun execute(binding: MacroController, context: IReplExecutionContext) {
        val keyBinding = context.getParameter(bindingParameter)
        val command = context.getParameter(commandParameter)

        val existing = binding.macroList.find {
            it.keyBinding == keyBinding
        }?.commands ?: emptyList()

        binding.macroList.removeAll {
            it.keyBinding == keyBinding
        }
        val macro = MacroController.Macro(keyBinding, existing + command.value)
        binding.macroList += macro
        binding.save()

        context.writeln(macro.keyBinding.toString())
        for (c in macro.commands) {
            context.write("  ")
            context.writeHighlightCommand(c)
            context.writeln()
        }
    }
}
