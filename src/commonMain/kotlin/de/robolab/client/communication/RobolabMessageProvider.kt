package de.robolab.client.communication

import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse
import de.robolab.client.communication.mqtt.MqttMessage
import de.robolab.client.communication.mqtt.RobolabMqttConnection
import de.robolab.client.net.http
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.utils.Logger
import de.robolab.common.utils.RobolabJson
import de.westermann.kobserve.event.SuspendEventHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author leon
 */

class RobolabMessageProvider(private val mqttConnection: RobolabMqttConnection) {

    private val logger = Logger(this)

    val onMessage = SuspendEventHandler<RobolabMessage>()
    val onMessageList = SuspendEventHandler<List<RobolabMessage>>()

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


    fun onMessage(message: MqttMessage) {
        val robolabMessage = parseMqttMessage(message) ?: return
        onRobolabMessage(robolabMessage)
    }

    private fun parseMqttLogLine(line: String): RobolabMessage.Metadata? {
        val match = MQTT_LOG_LINE.matchEntire(line) ?: return null
        val rawDateStr = match.groupValues.getOrNull(1) ?: return null
        val rawTopicStr = match.groupValues.getOrNull(2) ?: return null
        val rawContentStr = match.groupValues.getOrNull(3) ?: return null

        val date = MQTT_LOG_DATE_FORMAT.parse(rawDateStr)
        val topic = rawTopicStr.split(',').joinToString("/") { it.drop(3).dropLast(3) }
        val content = rawContentStr.replace("\\\"", "\"").replace("\\\\", "\\")

        return RobolabMessage.Metadata(
            date.utc.unixMillis.toLong(),
            topic.substringAfterLast('/').substringAfterLast('-'),
            From.UNKNOWN,
            topic,
            content.replace("\\n", "\n")
        )
    }

    suspend fun importMqttLog(log: Sequence<String>) {
        val sequence = log
            .mapNotNull { parseMqttLogLine(it) }
            .mapNotNull { parseMessage(it) }
            .chunked(100)

        for (chunk in sequence) {
            withContext(Dispatchers.Main) {
                onMessageList.emit(chunk)
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
                    importMqttLog(response.body.splitToSequence('\n'))
                }
            }
        } catch (e: Exception) {
            logger.warn { "Cannot load mqtt log!" }
        }
    }

    private fun onRobolabMessage(robolabMessage: RobolabMessage) {
        GlobalScope.launch(Dispatchers.Main) {
            onMessage.emit(robolabMessage)
        }
    }

    companion object {
        private val MQTT_LOG_LINE = """^([0-9:. -]*) \[info].*\[on_publish].*\[((?:<<.*>>)*)].*<<"(.*)">>$""".toRegex()
        private val MQTT_LOG_DATE_FORMAT = DateFormat("yyyy-MM-dd HH:mm:ss.SSS")

        private val logger = Logger("RobolabMessageParser")

        fun parseMessage(metadata: RobolabMessage.Metadata): RobolabMessage? {
            val jsonMessage = try {
                RobolabJson.decodeFromString(JsonMessage.serializer(), metadata.rawMessage)
            } catch (e: Exception) {
                logger.warn { "Group ${metadata.groupId} " + e.message }
                return RobolabMessage.IllegalMessage(
                    metadata,
                    RobolabMessage.IllegalMessage.Reason.NotParsable,
                    e.message
                )
            }

            val metadataWithFrom = metadata.copy(from = jsonMessage.from)

            val robolabMessage = try {
                jsonMessage.type.parseMessage(
                    metadataWithFrom,
                    jsonMessage
                )
            } catch (e: IllegalFromException) {
                logger.warn { "Group ${metadataWithFrom.groupId}: " + "Illegal \"from\" value (${e.actualFrom}) for message type ${e.messageType} in message ${metadataWithFrom.rawMessage}" }
                RobolabMessage.IllegalMessage(metadataWithFrom, RobolabMessage.IllegalMessage.Reason.IllegalFromValue)
            } catch (e: MissingJsonArgumentException) {
                logger.warn { "Group ${metadataWithFrom.groupId}: " + "Missing argument \"${e.argumentName}\" in message ${metadataWithFrom.rawMessage}" }
                RobolabMessage.IllegalMessage(
                    metadataWithFrom,
                    RobolabMessage.IllegalMessage.Reason.MissingArgument(e.argumentName)
                )
            } catch (e: IgnoreMessageException) {
                return null
            } catch (e: WrongTopicException) {
                RobolabMessage.IllegalMessage(metadataWithFrom, RobolabMessage.IllegalMessage.Reason.WrongTopic)
            }

            return robolabMessage
        }

        fun parseMqttMessage(message: MqttMessage): RobolabMessage? {
            val metadata = RobolabMessage.Metadata(
                message.timeArrived,
                message.topic.substringAfterLast('/').substringAfterLast('-'),
                From.UNKNOWN,
                message.topic,
                message.message
            )

            return parseMessage(metadata)
        }
    }
}
