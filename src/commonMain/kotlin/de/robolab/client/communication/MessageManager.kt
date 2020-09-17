package de.robolab.client.communication

import de.westermann.kobserve.event.SuspendEventHandler

/**
 * @author leon
 */
class
MessageManager(private val messageProvider: RobolabMessageProvider) {

    val onMessage = SuspendEventHandler<RobolabMessage>()
    val onMessageList = SuspendEventHandler<List<RobolabMessage>>()

    init {
        messageProvider.onMessage += this::onRobolabMessage
        messageProvider.onMessageList += this::onRobolabMessageList
    }

    private suspend fun onRobolabMessage(message: RobolabMessage) {
        onMessage.emit(message)
    }

    private suspend fun onRobolabMessageList(message: List<RobolabMessage>) {
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
