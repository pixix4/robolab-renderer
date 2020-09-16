package de.robolab.client.app.controller

import de.robolab.client.communication.mqtt.RobolabMqttConnection
import de.westermann.kobserve.property.mapBinding

class StatusBarController(
    canvasController: CanvasController,
    private val connection: RobolabMqttConnection
) {

    val entryListProperty = canvasController.pointerStringProperty


    val statusColor = connection.connectionStateProperty.mapBinding {
        when (it) {
            is RobolabMqttConnection.Connected -> StatusColor.SUCCESS
            is RobolabMqttConnection.Connecting -> StatusColor.WARN
            is RobolabMqttConnection.ConnectionLost -> StatusColor.ERROR
            is RobolabMqttConnection.Disconnected -> StatusColor.ERROR
            else -> StatusColor.ERROR
        }
    }

    val statusMessage = connection.connectionStateProperty.mapBinding {
        it.name
    }

    val statusActionLabel = connection.connectionStateProperty.mapBinding {
        it.actionLabel
    }

    fun onStatusAction() {
        connection.connectionState.onAction()
    }

    enum class StatusColor {
        SUCCESS,
        WARN,
        ERROR
    }
}
