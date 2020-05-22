package de.robolab.client.communication.mqtt

actual class MqttClient actual constructor(serverUri: String, clientId: String) {
    actual fun connect(username: String, password: String) {
        throw UnsupportedOperationException()
    }

    actual fun disconnect() {
        throw UnsupportedOperationException()
    }

    actual fun subscribe(topic: String) {
        throw UnsupportedOperationException()
    }

    actual fun unsubscribe(topic: String) {
        throw UnsupportedOperationException()
    }

    actual fun publish(topic: String, message: String) {
        throw UnsupportedOperationException()
    }

    actual fun setCallback(callback: Callback?) {
        throw UnsupportedOperationException()
    }

    actual interface Callback {
        actual fun onConnect()
        actual fun onConnectionLost()
        actual fun onMessage(topic: String, message: String)
    }

}