package de.robolab.client.repl.commands.macro

import de.robolab.client.app.controller.MacroController
import de.robolab.client.repl.base.IReplExecutionContext
import de.robolab.client.repl.base.IntParameter
import de.robolab.client.repl.base.ReplBindableLeafCommand

object MacroRemoveCommand : ReplBindableLeafCommand<MacroController>(
    "remove",
    "Remove an existing command",
    MacroController::class,
) {

    private val bindingParameter = MacroController.KeyBinding.param("binding")
    private val indexParameter = IntParameter.optional("index")

    override suspend fun execute(binding: MacroController, context: IReplExecutionContext) {
        val keyBinding = context.getParameter(bindingParameter)
        val index = context.getParameter(indexParameter)
        val existing = binding.macroList.find {
            it.keyBinding == keyBinding
        }?.commands ?: emptyList()


        binding.macroList.removeAll {
            it.keyBinding == keyBinding
        }

        if (index != null && index.value >= 0 && index.value < existing.size && existing.size > 1) {
            val l = existing.toMutableList()
            l.removeAt(index.value)

            val macro = MacroController.Macro(keyBinding, l)
            binding.macroList += macro

            context.writeln(macro.keyBinding.toString())
            for (c in macro.commands) {
                context.write("  ")
                context.writeHighlightCommand(c)
                context.writeln()
            }
        }

        binding.save()
    }
}
