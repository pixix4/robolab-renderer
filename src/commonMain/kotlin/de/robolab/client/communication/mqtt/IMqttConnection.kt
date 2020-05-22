package de.robolab.client.communication.mqtt

import de.westermann.kobserve.event.EventHandler

interface IMqttConnection {

    val subscribedTopics: Set<String>

    val onMessage: EventHandler<MqttMessage>

    fun connect(): Boolean
    fun disconnect(): Boolean
    fun subscribe(topic: String): Boolean
    fun unsubscribe(topic: String): Boolean
    fun publish(topic: String, message: String): Boolean
}
