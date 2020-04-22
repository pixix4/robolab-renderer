package de.robolab.communication.mqtt

expect class MqttClient(serverUri: String, clientId: String) {

    fun connect(username: String, password: String)
    fun disconnect()

    fun subscribe(topic: String)
    fun unsubscribe(topic: String)

    fun publish(topic: String, message: String)

    fun setCallback(callback: Callback?)
    interface Callback {
        fun onConnect()
        fun onConnectionLost()
        fun onMessage(topic: String, message: String)
    }
}
