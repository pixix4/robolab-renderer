package de.robolab.client.communication

import de.robolab.client.communication.mqtt.MqttMessage
import de.robolab.client.communication.mqtt.RobolabMqttConnection
import de.robolab.client.net.http
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.utils.Logger
import de.robolab.common.utils.RobolabJson
import de.robolab.common.utils.parseDateTime
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

        val date = parseDateTime(rawDateStr, MQTT_LOG_DATE_FORMAT)
        val topic = rawTopicStr.split(',').joinToString("/") { it.drop(3).dropLast(3) }
        val content = rawContentStr.replace("\\\"", "\"").replace("\\\\", "\\")

        return RobolabMessage.Metadata(
            date.toEpochMilliseconds(),
            topic.substringAfterLast('/').substringAfterLast('-'),
            From.UNKNOWN,
            topic,
            content.replace("\\n", "\n").replace("\\t", "\t")
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
                if (PreferenceStorage.logUri.contains("?count=")) {
                    PreferenceStorage.logUri = PreferenceStorage.logUri.substringBefore("?count=")
                }
                val logUri = "${PreferenceStorage.logUri}?count=${PreferenceStorage.logCount}"
                val response = http {
                    url(logUri)
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
        @Suppress("RegExpRedundantEscape")
        private val MQTT_LOG_LINE = """^([0-9:. -]*) \[info\].*\[on_publish\].*\[((?:<<.*>>)*)\].*<<"(.*)">>$""".toRegex()
        private const val MQTT_LOG_DATE_FORMAT = "YYYY-MM-DD HH:mm:ss.SSS"

        private val logger = Logger("RobolabMessageParser")

        fun parseMessage(metadata: RobolabMessage.Metadata): RobolabMessage? {
            var parsedMetadata: RobolabMessage.Metadata = metadata

            return try {
                when {
                    metadata.topic.startsWith("stats/") -> {
                        val (statsMessage, readChannel) = try {
                            StatsMessage.decode(metadata.rawMessage)
                        } catch(e: Exception){
                            logger.warn { "Group ${metadata.groupId} " + e.message }
                            return RobolabMessage.IllegalMessage(
                                metadata,
                                RobolabMessage.IllegalMessage.Reason.NotParsable,
                                e.message
                            )
                        }

                        statsMessage.type.parseMessage(parsedMetadata, statsMessage, readChannel)
                    }
                    else -> {
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

                        parsedMetadata = metadata.copy(from = jsonMessage.from)

                        try {
                            jsonMessage.type.parseMessage(parsedMetadata, jsonMessage)
                        } catch (e: IllegalFromException) {
                            logger.warn { "Group ${parsedMetadata.groupId}: " + "Illegal \"from\" value (${e.actualFrom}) for message type ${e.messageType} in message ${parsedMetadata.rawMessage}" }
                            RobolabMessage.IllegalMessage(
                                parsedMetadata,
                                RobolabMessage.IllegalMessage.Reason.IllegalFromValue
                            )
                        } catch (e: MissingJsonArgumentException) {
                            logger.warn { "Group ${parsedMetadata.groupId}: " + "Missing argument \"${e.argumentName}\" in message ${parsedMetadata.rawMessage}" }
                            RobolabMessage.IllegalMessage(
                                parsedMetadata,
                                RobolabMessage.IllegalMessage.Reason.MissingArgument(e.argumentName)
                            )
                        }
                    }
                }
            } catch (e: IgnoreMessageException) {
                return null
            } catch (e: WrongTopicException) {
                RobolabMessage.IllegalMessage(parsedMetadata, RobolabMessage.IllegalMessage.Reason.WrongTopic)
            }
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
