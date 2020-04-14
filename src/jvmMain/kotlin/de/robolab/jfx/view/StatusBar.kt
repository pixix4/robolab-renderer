package de.robolab.jfx.view

import de.robolab.app.controller.StatusBarController
import de.robolab.jfx.style.MainStyle
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
