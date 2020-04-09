package de.robolab.communication

import kotlin.reflect.KProperty0

class IllegalFromException(
        val actualFrom: From,
        val messageType: Type
) : Exception()

fun <T> KProperty0<T?>.parsed() = this.get() ?: throw MissingJsonArgumentException(this.name)

infix fun <T> KProperty0<T?>.orElse(value: T) = this.get()
        ?: value.also { println("Attribute $name is missing.") }

class MissingJsonArgumentException(val name: String) : Exception()

class IgnoreMessageException : Exception()
class WrongTopicException(val topic: String, messageType: Type) : Exception()

enum class Topic(val topicName: String) {
    EXPLORER("explorer"),
    PLANET("planet"),
    CONTROLLER("controller")
}

fun RobolabMessage.Metadata.requireTopic(requiredTopic: Topic, type: Type) {
    if (requiredTopic == Topic.CONTROLLER) {
        if (!topic.startsWith(Topic.CONTROLLER.topicName)) {
            throw IgnoreMessageException()
        }
    } else {
        if (topic.startsWith(Topic.CONTROLLER.topicName)) {
            throw IgnoreMessageException()
        }
        if (!topic.startsWith(requiredTopic.topicName)) {
            throw WrongTopicException(topic, type)
        }
    }
}