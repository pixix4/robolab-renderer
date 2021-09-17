package de.robolab.client.repl.commands.mqtt

import de.robolab.client.app.controller.ConnectionController
import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.repl.ReplExecutor
import de.robolab.client.repl.base.ReplCommandParameterDescriptor
import de.robolab.client.repl.base.ReplSingleBindableNodeCommand
import de.robolab.client.repl.commands.planet.edit.EditCommand
import de.robolab.client.repl.commands.planet.generate.GenerateCommand
import de.robolab.common.planet.PlanetPathVertex

object MqttCommand : ReplSingleBindableNodeCommand<ConnectionController>(
    "mqtt",
    "Connect to the mothership via mqtt",
    ConnectionController::class,
) {

    init {
        addCommand(MqttConnectCommand)
        addCommand(MqttDisconnectCommand)
        addCommand(MqttAbortCommand)
    }
}
