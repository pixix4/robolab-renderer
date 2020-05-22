package de.robolab.client.communication.mqtt

import javafx.application.Platform
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

actual class MqttClient actual constructor(serverUri: String, clientId: String) {

    private var callback: Callback? = null

    private val client = MqttClient(serverUri, clientId, MemoryPersistence())

    actual fun connect(username: String, password: String) {
        client.connect(MqttConnectOptions().apply {
            userName = username
            this.password = password.toCharArray()
            mqttVersion = MqttConnectOptions.MQTT_VERSION_3_1
            connectionTimeout = 5
            isCleanSession = false
            keepAliveInterval = 5
        })

        Platform.runLater {
            callback?.onConnect()
        }
    }

    actual fun disconnect() {
        client.disconnect()
    }

    actual fun subscribe(topic: String) {
        client.subscribe(topic)
    }

    actual fun unsubscribe(topic: String) {
        client.unsubscribe(topic)
    }

    actual fun publish(topic: String, message: String) {
        client.publish(
                topic,
                message.toByteArray(),
                1,
                false
        )
    }

    actual fun setCallback(callback: Callback?) {
        this.callback = callback
    }

    init {
        client.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                val msgTopic = topic ?: return
                val msgPayload = message?.payload?.let { String(it) } ?: return
                Platform.runLater {
                    callback?.onMessage(
                            msgTopic,
                            msgPayload
                    )
                }
            }

            override fun connectionLost(cause: Throwable?) {
                Platform.runLater {
                    callback?.onConnectionLost()
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
            }

        })
    }

    actual interface Callback {
        actual fun onConnect()
        actual fun onConnectionLost()
        actual fun onMessage(topic: String, message: String)
    }
}
