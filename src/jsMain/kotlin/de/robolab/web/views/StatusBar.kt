package de.robolab.web.views

import de.robolab.app.controller.StatusBarController
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.TextView
import de.westermann.kwebview.components.textView
import de.westermann.kwebview.extra.listFactory

class StatusBar(private val statusBarController: StatusBarController, sideBarActiveProperty: ReadOnlyProperty<Boolean>) : ViewCollection<View>() {

    private fun updateStatusBar() {
        clear()
        for (item in statusBarController.entryListProperty.value) {
            textView(item)
        }
    }

    init {
        classList.bind("side-bar-active", sideBarActiveProperty)

        updateStatusBar()
        statusBarController.entryListProperty.onChange {
            updateStatusBar()
        }
    }
}
