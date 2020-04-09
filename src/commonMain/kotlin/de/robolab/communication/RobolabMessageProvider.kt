package de.robolab.communication

import de.robolab.communication.mqtt.MqttMessage
import de.robolab.communication.mqtt.RobolabMqttConnection
import de.westermann.kobserve.event.EventHandler
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

/**
 * @author leon
 */

class RobolabMessageProvider(private val mqttConnection: RobolabMqttConnection){
    
    val onMessage = EventHandler<RobolabMessage>()

    init {
        mqttConnection.onMessage += this::onMessage
    }

    fun start() = if (mqttConnection.connect()) {
        mqttConnection.subscribe("#")
        true
    } else {
        false
    }

    fun stop() {
        mqttConnection.disconnect()
    }

    private val jsonSerializer = Json(JsonConfiguration.Stable)

    private fun onMessage(message: MqttMessage) {
        val groupId = message.topic.substringAfterLast('/').substringAfterLast('-')

        var metadata = RobolabMessage.Metadata(
                message.timeArrived,
                groupId,
                From.UNKNOWN,
                message.topic,
                message.message
        )


        val jsonMessage = try {
            jsonSerializer.parse(JsonMessage.serializer(), message.message)
        } catch (e: Exception) {
            //e.printStackTrace()
            onRobolabMessage(RobolabMessage.IllegalMessage(
                    metadata,
                    RobolabMessage.IllegalMessage.Reason.NotParsable,
                    e.message
            ))
            return
        }

        metadata = metadata.copy(from = jsonMessage.from)

        val robolabMessage = try {
            jsonMessage.type.parseMessage(
                    metadata,
                    jsonMessage
            )
        } catch (e: IllegalFromException) {
            println("Illegal \"from\" value (${e.actualFrom}) for message type ${e.messageType} in message ${metadata.rawMessage}")
            RobolabMessage.IllegalMessage(metadata, RobolabMessage.IllegalMessage.Reason.IllegalFromValue)
        } catch (e: MissingJsonArgumentException) {
            println("Missing argument \"${e.name}\" in message ${metadata.rawMessage}")
            RobolabMessage.IllegalMessage(metadata, RobolabMessage.IllegalMessage.Reason.MissingArgument(e.name))
        } catch (e: IgnoreMessageException) {
            return
        } catch (e: WrongTopicException) {
            RobolabMessage.IllegalMessage(metadata, RobolabMessage.IllegalMessage.Reason.WrongTopic)
        }
        onRobolabMessage(robolabMessage)
    }

    private fun onRobolabMessage(robolabMessage: RobolabMessage) {
        onMessage.emit(robolabMessage)
    }
}
