package de.robolab.communication.mqtt

import com.soywiz.klock.DateTime
import de.robolab.utils.Logger
import de.robolab.utils.PreferenceStorage
import de.robolab.utils.runAfterTimeout
import de.robolab.utils.runAsync
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.property.property
import kotlin.math.min

/**
 * @author leon
 */
class RobolabMqttConnection : IMqttConnection {
    
    private val logger = Logger(this)

    private val subscribedTopicsProperty = property(emptySet<String>())
    override var subscribedTopics: Set<String> by subscribedTopicsProperty


    override val onMessage = EventHandler<MqttMessage>()
    val onConnectionLost = EventHandler<Long>()


    override fun connect() = connectionState.connect()
    override fun disconnect() = connectionState.disconnect()
    override fun subscribe(topic: String): Boolean = connectionState.subscribe(topic)
    override fun unsubscribe(topic: String): Boolean = connectionState.unsubscribe(topic)
    override fun publish(topic: String, message: String): Boolean = connectionState.publish(topic, message)

    companion object {
        const val MAXIMUM_RECONNECT_TIME = 180000L
        const val INITIAL_RECONNECT_TIME = 1000L
    }

    val connectionStateProperty = property<ConnectionState>(Disconnected())
    private var connectionState: ConnectionState by connectionStateProperty

    init {
        connectionStateProperty.onChange {
            logger.debug { "Set connection state to ${connectionState.name}" }
        }
    }

    abstract class ConnectionState {
        val name: String = this::class.simpleName ?: ""

        open fun connect() = false
        open fun reconnect() = false
        open fun onConnected() = false
        open fun disconnect() = false
        open fun onConnectionLost() = false
        open fun onMessage(topic: String, message: String) = false
        open fun subscribe(topic: String) = false
        open fun unsubscribe(topic: String) = false
        open fun publish(topic: String, message: String) = false
    }


    inner class Connected(
            private val mqttClient: MqttClient
    ) : ConnectionState() {

        override fun disconnect(): Boolean {
            subscribedTopics = emptySet()
                mqttClient.disconnect()
                connectionState = Disconnected()
            return true
        }

        override fun onConnectionLost(): Boolean {
            connectionState = ConnectionLost(INITIAL_RECONNECT_TIME)
            connectionState.reconnect()
            return true
        }

        override fun onMessage(topic: String, message: String): Boolean {
            val time = DateTime.now().unixMillisLong
            onMessage.emit(MqttMessage(time, topic, message))
            return true
        }

        override fun subscribe(topic: String): Boolean {
            logger.info { "Subscribing to $topic" }
            mqttClient.subscribe(topic)
            logger.info { "Subscribed to $topic" }
            subscribedTopics = subscribedTopics + topic
            return true
        }

        override fun unsubscribe(topic: String): Boolean {
            mqttClient.unsubscribe(topic)
            subscribedTopics = subscribedTopics - topic
            return true
        }

        override fun publish(topic: String, message: String): Boolean {
            mqttClient.publish(topic, message)
            return true
        }
    }

    inner class Disconnected : ConnectionState() {
        override fun connect(): Boolean {
            connectionState = Connecting(INITIAL_RECONNECT_TIME)
            connectionState.connect()
            return true
        }
    }

    inner class Connecting(
            private val timeUntilReconnect: Long
    ) : ConnectionState() {

        private val mqttClient = MqttClient(PreferenceStorage.serverUri, PreferenceStorage.clientId).also {
            logger.info { "clientId = ${PreferenceStorage.clientId}" }
        }

        override fun connect(): Boolean {
            mqttClient.setCallback(object : MqttClient.Callback {
                override fun onConnect() {
                    connectionState.onConnected()
                }

                override fun onConnectionLost() {
                    logger.warn { "Connection lost" }
                    connectionState.onConnectionLost()
                }

                override fun onMessage(topic: String, message: String) {
                    connectionState.onMessage(topic, message)
                }
            })
            logger.info { "Connecting to ${PreferenceStorage.serverUri}" }
            runAsync {
                try {
                    mqttClient.connect(
                            PreferenceStorage.username,
                            PreferenceStorage.password
                    )
                    connectionState.onConnected()
                } catch (exception: Exception) {
                        connectionState.onConnectionLost()
                }
            }

            return true
        }

        override fun subscribe(topic: String): Boolean {
            subscribedTopics = subscribedTopics + topic
            return true
        }

        override fun disconnect(): Boolean {
            mqttClient.disconnect()
            connectionState = Disconnected()
            return true
        }

        override fun onConnected(): Boolean {
            connectionState = Connected(mqttClient)
            subscribedTopics.forEach {
                connectionState.subscribe(it)
            }
            return true
        }

        override fun onConnectionLost(): Boolean {
            connectionState = ConnectionLost(timeUntilReconnect)
            connectionState.reconnect()
            return true
        }
    }

    inner class ConnectionLost(private val timeUntilReconnect: Long) : ConnectionState() {
        override fun reconnect(): Boolean {
            logger.info { "Reconnecting in ${timeUntilReconnect / 1000} seconds" }
            runAfterTimeout(timeUntilReconnect) {
                if (connectionState == this@ConnectionLost) {
                    connectionState = Connecting(min(MAXIMUM_RECONNECT_TIME, timeUntilReconnect * 2))
                    connectionState.connect()
                }
            }
            onConnectionLost.emit(timeUntilReconnect)
            return true
        }

        override fun connect(): Boolean {
            connectionState = Connecting(INITIAL_RECONNECT_TIME)
            connectionState.connect()
            return true

        }

        override fun disconnect(): Boolean {
            connectionState = Disconnected()
            return true
        }
    }
}
