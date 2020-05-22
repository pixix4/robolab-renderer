package de.robolab.client.jfx.utils

import de.robolab.client.jfx.style.MainStyle
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.layout.HBox
import tornadofx.*

fun EventTarget.buttonGroup(init: HBox.() -> Unit) {
    hbox {
        addClass(MainStyle.buttonGroup)

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
