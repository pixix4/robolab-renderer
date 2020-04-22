package de.robolab.communication

import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.list.observableListOf

/**
 * @author leon
 */
class MessageManager(private val messageProvider: RobolabMessageProvider) {

    val messageList = observableListOf<RobolabMessage>()
    val onMessage = EventHandler<RobolabMessage>()

    init {
        messageProvider.onMessage += this::onRobolabMessage
    }

    private fun onRobolabMessage(message: RobolabMessage) {
        messageList += message
        onMessage.emit(message)
    }
}
