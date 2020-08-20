package de.robolab.client.communication

import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse
import de.robolab.client.communication.mqtt.MqttMessage
import de.robolab.client.communication.mqtt.RobolabMqttConnection
import de.robolab.client.net.http
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.utils.Logger
import de.westermann.kobserve.event.EventHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * @author leon
 */

class RobolabMessageProvider(private val mqttConnection: RobolabMqttConnection) {

    private val logger = Logger(this)

    val onMessage = EventHandler<RobolabMessage>()
    val onMessageList = EventHandler<List<RobolabMessage>>()

    private var logLoaded = false

    fun sendMessage(topic: String, message: String): Boolean {
        return mqttConnection.sendMessage(topic, message)
    }

    init {
        mqttConnection.onMessage += this::onMessage

        mqttConnection.connectionStateProperty.onChange {
            if (mqttConnection.connectionState is RobolabMqttConnection.Connected && !logLoaded) {
                logLoaded = true

                loadMqttLog()
            }
        }
    }

    private val jsonSerializer = Json { }

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
            jsonSerializer.decodeFromString(JsonMessage.serializer(), message.message)
        } catch (e: Exception) {
            logger.warn { "Group $groupId: " + e.message }
            onRobolabMessage(
                RobolabMessage.IllegalMessage(
                    metadata,
                    RobolabMessage.IllegalMessage.Reason.NotParsable,
                    e.message
                )
            )
            return null
        }

        metadata = metadata.copy(from = jsonMessage.from)

        val robolabMessage = try {
            jsonMessage.type.parseMessage(
                metadata,
                jsonMessage
            )
        } catch (e: IllegalFromException) {
            logger.warn { "Group $groupId: " + "Illegal \"from\" value (${e.actualFrom}) for message type ${e.messageType} in message ${metadata.rawMessage}" }
            RobolabMessage.IllegalMessage(metadata, RobolabMessage.IllegalMessage.Reason.IllegalFromValue)
        } catch (e: MissingJsonArgumentException) {
            logger.warn { "Group $groupId: " + "Missing argument \"${e.argumentName}\" in message ${metadata.rawMessage}" }
            RobolabMessage.IllegalMessage(
                metadata,
                RobolabMessage.IllegalMessage.Reason.MissingArgument(e.argumentName)
            )
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
            date.utc.unixMillis.toLong(),
            topic,
            content.replace("\\n", "\n")
        )
    }

    suspend fun importMqttLog(log: String) {
        log.splitToSequence('\n')
            .asFlow()
            .mapNotNull { parseMqttLogLine(it) }
            .mapNotNull { parseMqttMessage(it) }
            .collect { message ->
                withContext(Dispatchers.Main) {
                    onMessage.emit(message)
                }
            }
    }

    private fun loadMqttLog() {
        try {
            GlobalScope.launch(Dispatchers.Default) {
                val response = http {
                    url(PreferenceStorage.logUri)
                }.exec()

                if (response.body == null) {
                    logger.warn { "Cannot load mqtt log!" }
                } else {
                    importMqttLog(response.body)
                }
            }
        } catch (e: Exception) {
            logger.warn { "Cannot load mqtt log!" }
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
