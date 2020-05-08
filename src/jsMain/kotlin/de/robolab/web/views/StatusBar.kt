package de.robolab.web.views

import de.robolab.app.controller.StatusBarController
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.textView

class StatusBar(private val statusBarController: StatusBarController, sideBarActiveProperty: ObservableValue<Boolean>) : ViewCollection<View>() {

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
