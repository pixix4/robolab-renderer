package de.robolab.client.ui.utils

import de.robolab.client.ui.style.MainStyle
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.layout.HBox
import tornadofx.*

fun EventTarget.buttonGroup(inputButtonGroup: Boolean = false, init: HBox.() -> Unit) {
    hbox {
        addClass(MainStyle.buttonGroup)
        if (inputButtonGroup) {
            addClass(MainStyle.inputButtonGroup)
        }

        init()

        children.bindFirstLastPseudoElements()
    }
}

fun ObservableList<Node>.bindFirstLastPseudoElements() {
    fun update() {
        forEach {
            it.removePseudoClass("first")
            it.removePseudoClass("last")
        }

        firstOrNull()?.addPseudoClass("first")
        lastOrNull()?.addPseudoClass("last")
    }

    update()
    onChange {
        update()
    }
}
