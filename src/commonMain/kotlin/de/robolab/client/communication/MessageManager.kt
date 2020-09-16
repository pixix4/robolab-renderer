package de.robolab.client.communication

import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.list.observableListOf

/**
 * @author leon
 */
class
MessageManager(private val messageProvider: RobolabMessageProvider) {

    val onMessage = EventHandler<RobolabMessage>()
    val onMessageList = EventHandler<List<RobolabMessage>>()

    init {
        messageProvider.onMessage += this::onRobolabMessage
        messageProvider.onMessageList += this::onRobolabMessageList
    }

    private fun onRobolabMessage(message: RobolabMessage) {
        onMessage.emit(message)
    }

    private fun onRobolabMessageList(message: List<RobolabMessage>) {
        onMessageList.emit(message)
    }

    fun sendMessage(topic: String, message: String): Boolean {
        return try {
            messageProvider.sendMessage(topic, message)
            true
        } catch (e: Exception) {
            false
        }
    }
}
