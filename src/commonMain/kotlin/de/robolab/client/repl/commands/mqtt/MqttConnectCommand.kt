package de.robolab.client.repl.commands.mqtt

import de.robolab.client.app.controller.ConnectionController
import de.robolab.client.communication.mqtt.RobolabMqttConnection
import de.robolab.client.repl.base.IReplExecutionContext
import de.robolab.client.repl.base.ReplBindableLeafCommand

object MqttConnectCommand: ReplBindableLeafCommand<ConnectionController>(
    "connect",
    "Connect to the mothership",
    ConnectionController::class,
) {

    override suspend fun execute(binding: ConnectionController, context: IReplExecutionContext) {
        val state = binding.connection.connectionState

        if (state is RobolabMqttConnection.Disconnected || state is RobolabMqttConnection.ConnectionLost) {
            state.onAction()
        }
    }
}
