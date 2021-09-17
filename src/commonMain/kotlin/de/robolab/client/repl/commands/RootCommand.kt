package de.robolab.client.repl.commands

import de.robolab.client.repl.ReplExecutor
import de.robolab.client.repl.autoComplete
import de.robolab.client.repl.base.BooleanParameter
import de.robolab.client.repl.base.ReplCommandParameterDescriptor
import de.robolab.client.repl.base.ReplSingleBindableNodeCommand
import de.robolab.client.repl.commands.macro.MacroCommand
import de.robolab.client.repl.commands.mqtt.MqttCommand
import de.robolab.client.repl.commands.planet.PlanetCommand
import de.robolab.client.repl.commands.window.WindowCommand

object RootCommand : ReplSingleBindableNodeCommand<Unit>(
    "root",
    "root",
    Unit::class,
) {

    init {
        addCommand(PlanetCommand)
        addCommand(WindowCommand)
        addCommand(MqttCommand)
        addCommand(MacroCommand)

        addCommand(HelpCommand)
        addCommand(TreeCommand)
    }

    override suspend fun requestAutoCompleteFor(
        binding: Unit,
        descriptor: ReplCommandParameterDescriptor<*>,
        token: String,
    ): List<ReplExecutor.AutoComplete>? {
        if (descriptor.type is BooleanParameter.Companion) {
            val values = listOf("true", "false")

            return values.autoComplete(token)
        }

        return null
    }
}
