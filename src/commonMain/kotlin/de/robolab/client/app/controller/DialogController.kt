package de.robolab.client.app.controller

import de.robolab.client.app.viewmodel.DialogViewModel
import de.westermann.kobserve.event.EventHandler

object DialogController {

    val onOpen = EventHandler<DialogViewModel>()

    fun open(dialog: DialogViewModel) {
        onOpen.emit(dialog)
    }
}