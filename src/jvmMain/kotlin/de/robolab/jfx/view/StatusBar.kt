package de.robolab.jfx.view

import de.robolab.app.controller.StatusBarController
import de.robolab.jfx.adapter.toFx
import de.westermann.kobserve.property.mapBinding
import tornadofx.*

class StatusBar(statusBarController: StatusBarController) : View() {

    override val root = hbox {
        label(statusBarController.entryListProperty.mapBinding { it.joinToString(" | ") }.toFx())
    }
}
