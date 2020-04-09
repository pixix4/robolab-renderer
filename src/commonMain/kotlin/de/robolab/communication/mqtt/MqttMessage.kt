package de.robolab.communication.mqtt

data class MqttMessage(
        val timeArrived: Long,
        val topic: String,
        val message: String
)
