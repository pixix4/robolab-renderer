package de.robolab.client.repl.commands.macro

import de.robolab.client.app.controller.MacroController
import de.robolab.client.repl.base.BooleanParameter
import de.robolab.client.repl.base.IReplExecutionContext
import de.robolab.client.repl.base.ReplBindableLeafCommand

object MacroDebugCommand : ReplBindableLeafCommand<MacroController>(
    "debug",
    "Log the currently pressed key binding",
    MacroController::class,
) {

    private val enabledParameter = BooleanParameter.optional("enabled")

    override suspend fun execute(binding: MacroController, context: IReplExecutionContext) {
        val enabled = context.getParameter(enabledParameter)

        when (enabled?.value) {
            null -> {
                binding.debugOutput = if (binding.debugOutput == null) {
                    context
                } else {
                    null
                }
            }
            true -> {
                if (binding.debugOutput == null) {
                    binding.debugOutput = context
                }
            }
            false -> {
                if (binding.debugOutput != null) {
                    binding.debugOutput = null
                }
            }
        }
    }
}
