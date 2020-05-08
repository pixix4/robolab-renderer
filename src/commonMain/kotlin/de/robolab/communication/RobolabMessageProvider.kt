package de.robolab.communication

import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse
import de.robolab.communication.mqtt.MqttMessage
import de.robolab.communication.mqtt.RobolabMqttConnection
import de.robolab.utils.Logger
import de.westermann.kobserve.event.EventHandler
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

/**
 * @author leon
 */

class RobolabMessageProvider(private val mqttConnection: RobolabMqttConnection) {

    private val logger = Logger(this)

    val onMessage = EventHandler<RobolabMessage>()
    val onMessageList = EventHandler<List<RobolabMessage>>()

    init {
        mqttConnection.onMessage += this::onMessage
    }

    private val jsonSerializer = Json(JsonConfiguration.Stable)

    private fun parseMqttMessage(message: MqttMessage): RobolabMessage? {
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
            logger.error { e }
            onRobolabMessage(RobolabMessage.IllegalMessage(
                    metadata,
                    RobolabMessage.IllegalMessage.Reason.NotParsable,
                    e.message
            ))
            return null
        }

        metadata = metadata.copy(from = jsonMessage.from)

        val robolabMessage = try {
            jsonMessage.type.parseMessage(
                    metadata,
                    jsonMessage
            )
        } catch (e: IllegalFromException) {
            logger.error { "Illegal \"from\" value (${e.actualFrom}) for message type ${e.messageType} in message ${metadata.rawMessage}" }
            RobolabMessage.IllegalMessage(metadata, RobolabMessage.IllegalMessage.Reason.IllegalFromValue)
        } catch (e: MissingJsonArgumentException) {
            logger.error { "Missing argument \"${e.name}\" in message ${metadata.rawMessage}" }
            RobolabMessage.IllegalMessage(metadata, RobolabMessage.IllegalMessage.Reason.MissingArgument(e.name))
        } catch (e: IgnoreMessageException) {
            return null
        } catch (e: WrongTopicException) {
            RobolabMessage.IllegalMessage(metadata, RobolabMessage.IllegalMessage.Reason.WrongTopic)
        }

        return robolabMessage
    }

    private fun onMessage(message: MqttMessage) {
        val robolabMessage = parseMqttMessage(message) ?: return
        onRobolabMessage(robolabMessage)
    }

    private fun parseMqttLogLine(line: String): MqttMessage? {
        val match = MQTT_LOG_LINE.matchEntire(line) ?: return null
        val rawDateStr = match.groupValues.getOrNull(1) ?: return null
        val rawTopicStr = match.groupValues.getOrNull(2) ?: return null
        val rawContentStr = match.groupValues.getOrNull(3) ?: return null

        val date = MQTT_LOG_DATE_FORMAT.parse(rawDateStr)
        val topic = rawTopicStr.split(',').joinToString("/") { it.drop(3).dropLast(3) }
        val content = rawContentStr.replace("\\\"", "\"").replace("\\\\", "\\")

        return MqttMessage(
                date.utc.unixMillisLong,
                topic,
                content
        )
    }

    fun loadMqttLog() {
        httpRequest("demo/mqtt.console.log") { content ->
            if (content == null) {
                logger.warn { "Cannot load mqtt.console.log!" }
            } else {
                val list = content.splitToSequence('\n').mapNotNull { parseMqttLogLine(it) }.mapNotNull { parseMqttMessage(it) }
                onMessageList.emit(list.toList())
            }
        }
    }

    private fun onRobolabMessage(robolabMessage: RobolabMessage) {
        onMessage.emit(robolabMessage)
    }

    companion object {
        private val MQTT_LOG_LINE = """^([0-9:. -]*) \[info].*\[on_publish].*\[((?:<<.*>>)*)].*<<"(.*)">>$""".toRegex()
        private val MQTT_LOG_DATE_FORMAT = DateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    }
}
