package de.robolab.client.communication

import de.robolab.common.utils.Logger
import kotlin.reflect.KProperty0

class IllegalFromException(
    val actualFrom: From,
    val messageType: Type
) : Exception()

private val logger = Logger("JsonExtensions")

fun <T> KProperty0<T?>.parsed() = this.get() ?: throw MissingJsonArgumentException(this.name)

infix fun <T> KProperty0<T?>.orElse(value: T) = this.get()
        ?: value.also { logger.warn { "Attribute $name is missing." } }

class MissingJsonArgumentException(val argumentName: String) : Exception()

class IgnoreMessageException : Exception()
class WrongTopicException(val topic: String, @Suppress("UNUSED_PARAMETER") messageType: Type) : Exception()

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