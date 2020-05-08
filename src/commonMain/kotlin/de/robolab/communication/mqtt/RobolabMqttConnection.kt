package de.robolab.communication.mqtt

import com.soywiz.klock.DateTime
import de.robolab.utils.Logger
import de.robolab.utils.PreferenceStorage
import de.robolab.utils.runAfterTimeout
import de.robolab.utils.runAsync
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.property

class RobolabMqttConnection {

    private val logger = Logger(this)

    val onMessage = EventHandler<MqttMessage>()

    val connectionStateProperty = property<ConnectionState>(Disconnected())
    val connectionState by connectionStateProperty

    interface ConnectionState {

        val name: String
        val actionLabel: String

        fun onAction()
    }

    inner class Disconnected : ConnectionState {
        override val name = "Disconnected"
        override val actionLabel = "Connect"

        override fun onAction() {
            connectionStateProperty.value = Connecting()
        }
    }

    inner class Connecting : ConnectionState {
        override val name = "Connecting"
        override val actionLabel = "Abort"

        override fun onAction() {
            mqttClient.setCallback(null)
            mqttClient.disconnect()
            connectionStateProperty.value = Disconnected()
        }

        private val mqttClient = MqttClient(PreferenceStorage.serverUri, PreferenceStorage.clientId)

        init {
            mqttClient.setCallback(object : MqttClient.Callback {
                override fun onConnect() {
                    connectionStateProperty.value = Connected(mqttClient)
                }

                override fun onConnectionLost() {
                    mqttClient.setCallback(null)
                    mqttClient.disconnect()
                    connectionStateProperty.value = ConnectionLost()
                }

                override fun onMessage(topic: String, message: String) {
                    val time = DateTime.nowUnixLong()
                    onMessage.emit(MqttMessage(time, topic, message))
                }
            })
            logger.info { "Connecting to ${PreferenceStorage.serverUri}" }
            runAsync {
                try {
                    mqttClient.connect(
                            PreferenceStorage.username,
                            PreferenceStorage.password
                    )
                } catch (exception: Exception) {
                    connectionStateProperty.value = ConnectionLost()
                }
            }
        }
    }

    inner class Connected(private val mqttClient: MqttClient) : ConnectionState {
        override val name = "Connected"

        override val actionLabel = "Disconnect"

        override fun onAction() {
            mqttClient.setCallback(null)
            mqttClient.disconnect()
            connectionStateProperty.value = Disconnected()
        }

        init {
            mqttClient.subscribe("#")
        }
    }

    inner class ConnectionLost : ConnectionState {
        override val name = "Connection lost"
        override val actionLabel = "Reconnect"

        private val timeoutReference = runAfterTimeout(5000) {
            connectionStateProperty.value = Connecting()
        }

        override fun onAction() {
            timeoutReference.cancel()
            connectionStateProperty.value = Connecting()
        }
    }
}
