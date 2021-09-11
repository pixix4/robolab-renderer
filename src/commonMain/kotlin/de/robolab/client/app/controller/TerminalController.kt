package de.robolab.client.app.controller

import de.westermann.kobserve.event.EventHandler

object TerminalController {

    val onExecute = EventHandler<String>()

    fun execute(input: String) {
        onExecute.emit(input)
    }
}
