package de.robolab.client.ui.view

import de.robolab.client.app.controller.StatusBarController
import de.robolab.client.ui.style.MainStyle
import javafx.scene.layout.HBox
import tornadofx.*

class StatusBar(private val statusBarController: StatusBarController) : View() {

    private fun updateStatusBar(box: HBox) {
        box.clear()

        for (element in statusBarController.entryListProperty.value) {
            box.label(element)
        }
    }

    override val root = hbox {
        addClass(MainStyle.statusBar)

        statusBarController.entryListProperty.onChange {
            updateStatusBar(this)
        }
        updateStatusBar(this)
    }
}
