package de.robolab.communication.mqtt

import de.robolab.communication.runDemo

actual class MqttClient actual constructor(serverUri: String, clientId: String) {

    private var callback: Callback? = null

    actual fun connect(username: String, password: String) {
        val c= callback ?:return

        c.onConnect()

        runDemo(c)
    }

    actual fun disconnect() {
    }

    actual fun subscribe(topic: String) {
    }

    actual fun unsubscribe(topic: String) {
    }

    actual fun publish(topic: String, message: String) {
    }

    actual fun setCallback(callback: Callback) {
        this.callback = callback
    }

    actual interface Callback {
        actual fun onConnect()
        actual fun onConnectionLost()
        actual fun onMessage(topic: String, message: String)
    }
}
