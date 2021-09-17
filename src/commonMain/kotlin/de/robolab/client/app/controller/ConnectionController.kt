package de.robolab.client.app.controller

import de.robolab.client.communication.mqtt.RobolabMqttConnection
import de.robolab.client.repl.commands.mqtt.MqttCommand
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding

class ConnectionController(
    val connection: RobolabMqttConnection,
    fileNavigationManager: RemoteServerController,
) {


    val connectionIndicatorList = observableListOf(
        ConnectionIndicator(
            "MQTT",
            connection.connectionStateProperty.mapBinding { it.name },
            connection.connectionStateProperty.mapBinding {
                when (it) {
                    is RobolabMqttConnection.Connected -> StatusColor.SUCCESS
                    is RobolabMqttConnection.Connecting -> StatusColor.WARN
                    is RobolabMqttConnection.ConnectionLost -> StatusColor.ERROR
                    is RobolabMqttConnection.Disconnected -> StatusColor.ERROR
                    else -> StatusColor.OTHER
                }
            },
            connection.connectionStateProperty.mapBinding {
                it.actionLabel
            }
        ) {
            connection.connectionState.onAction()
        },
        ConnectionIndicator(
            "Remote",
            fileNavigationManager.remoteServerAuthenticationProperty,
            fileNavigationManager.remotePlanetLoaderProperty.nullableFlatMapBinding { it?.availableProperty }
                .mapBinding {
                    when (it) {
                        null -> StatusColor.OTHER
                        false -> StatusColor.ERROR
                        true -> StatusColor.SUCCESS
                    }
                },
            constObservable("")
        ) {}
    )

    init {
        MqttCommand.bind(this)
    }

    enum class StatusColor {
        SUCCESS,
        WARN,
        ERROR,
        OTHER
    }

    class ConnectionIndicator(
        val name: String,
        val statusLabel: ObservableValue<String>,
        val statusColor: ObservableValue<StatusColor>,
        val actionLabel: ObservableValue<String>,
        val actionHandler: () -> Unit,
    )
}
