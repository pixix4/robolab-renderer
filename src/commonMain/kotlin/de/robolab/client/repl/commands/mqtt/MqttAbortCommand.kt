package de.robolab.client.repl.commands.mqtt

import de.robolab.client.app.controller.ConnectionController
import de.robolab.client.communication.mqtt.RobolabMqttConnection
import de.robolab.client.repl.base.IReplExecutionContext
import de.robolab.client.repl.base.ReplBindableLeafCommand

object MqttAbortCommand: ReplBindableLeafCommand<ConnectionController>(
    "abort",
    "Stop the current connection attempt",
    ConnectionController::class,
) {

    override suspend fun execute(binding: ConnectionController, context: IReplExecutionContext) {
        val state = binding.connection.connectionState

        if (state is RobolabMqttConnection.Connecting) {
            state.onAction()
        }
    }
}
