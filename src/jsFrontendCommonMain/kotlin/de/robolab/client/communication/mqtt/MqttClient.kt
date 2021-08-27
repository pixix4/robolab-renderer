package de.robolab.client.communication.mqtt

import de.robolab.client.communication.bindings.IClientOptions
import de.robolab.client.communication.bindings.MqttClient
import de.robolab.client.communication.bindings.connect
import de.robolab.client.utils.buildJsInterface

actual class MqttClient actual constructor(
        private val serverUri: String,
        private val clientId: String
) {

    private var callback: Callback? = null

    private fun onConnectCallback() {
        callback?.onConnect()
    }

    private fun onMessageCallback(topic: String, payload: dynamic) {
        callback?.onMessage(topic, payload.toString("utf8"))
    }

    private fun onDisconnectCallback() {
        callback?.onConnectionLost()
    }

    private var client: MqttClient? = null
    actual fun connect(username: String, password: String) {
        val id = clientId

        client = connect(serverUri, buildJsInterface<IClientOptions> {
            this.username = username
            this.password = password
            clientId = id
        })

        client?.on("connect", this::onConnectCallback)
        client?.on("message", this::onMessageCallback)
        client?.on("disconnect", this::onDisconnectCallback)
    }

    actual fun disconnect() {
        client?.end()
        client = null
    }

    actual fun subscribe(topic: String) {
        client?.subscribe(topic) { _, _ ->

        }
    }

    actual fun unsubscribe(topic: String) {
        client?.unsubscribe(topic)
    }

    actual fun publish(topic: String, message: String) {
        client?.publish(topic, message)
    }

    actual fun setCallback(callback: Callback?) {
        this.callback = callback
    }

    actual interface Callback {
        actual fun onConnect()
        actual fun onConnectionLost()
        actual fun onMessage(topic: String, message: String)
    }
}
