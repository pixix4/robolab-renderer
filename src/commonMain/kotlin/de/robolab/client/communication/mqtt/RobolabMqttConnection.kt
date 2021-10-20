package de.robolab.client.communication.mqtt

import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.utils.runAfterTimeout
import de.robolab.client.utils.runAsync
import de.robolab.common.utils.Logger
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.property
import kotlinx.datetime.Clock

class RobolabMqttConnection {

    private val logger = Logger(this)

    val onMessage = EventHandler<MqttMessage>()

    val connectionStateProperty = property<ConnectionState>(Disconnected())
    val connectionState by connectionStateProperty

    fun sendMessage(topic: String, message: String): Boolean {
        return connectionState.sendMessage(topic, message)
    }

    interface ConnectionState {

        val name: String
        val actionLabel: String

        fun onAction()
        fun sendMessage(topic: String, message: String): Boolean
    }

    inner class Disconnected : ConnectionState {
        override val name = "Disconnected"
        override val actionLabel = "Connect"

        override fun onAction() {
            connectionStateProperty.value = Connecting()
        }

        override fun sendMessage(topic: String, message: String): Boolean {
            return false
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

        override fun sendMessage(topic: String, message: String): Boolean {
            return false
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
                    val time = Clock.System.now().toEpochMilliseconds()
                    onMessage.emit(MqttMessage(time, topic, message.replace("\\n", "\n")))
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

        override fun sendMessage(topic: String, message: String): Boolean {
            return try {
                mqttClient.publish(topic, message)
                true
            } catch (e: Exception) {
                logger.error("Could not publish message", e)
                false
            }
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

        override fun sendMessage(topic: String, message: String): Boolean {
            return false
        }
    }
}
