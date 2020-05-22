package de.robolab.client.communication

import de.robolab.client.utils.runAfterTimeout
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.list.observableListOf

/**
 * @author leon
 */
class MessageManager(private val messageProvider: RobolabMessageProvider) {

    val messageList = observableListOf<RobolabMessage>()

    val onMessage = EventHandler<RobolabMessage>()
    val onMessageList = EventHandler<List<RobolabMessage>>()

    init {
        messageProvider.onMessage += this::onRobolabMessage
        messageProvider.onMessageList += this::onRobolabMessageList

        runAfterTimeout(1000) {
            messageProvider.loadMqttLog()
        }
    }

    private fun onRobolabMessage(message: RobolabMessage) {
        messageList += message
        onMessage.emit(message)
    }

    private fun onRobolabMessageList(message: List<RobolabMessage>) {
        messageList += message
        onMessageList.emit(message)
    }
}
