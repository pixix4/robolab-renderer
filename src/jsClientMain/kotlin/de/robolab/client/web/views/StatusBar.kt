package de.robolab.client.web.views

import de.robolab.client.app.controller.StatusBarController
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.textView

class StatusBar(
    private val statusBarController: StatusBarController,
    navigationBarActiveProperty: ObservableValue<Boolean>
) : ViewCollection<View>() {

    private fun updateStatusBar() {
        clear()
        for (item in statusBarController.entryListProperty.value) {
            textView(item)
        }
    }

    init {
        classList.bind("navigation-bar-active", navigationBarActiveProperty)

        updateStatusBar()
        statusBarController.entryListProperty.onChange {
            updateStatusBar()
        }
    }
}
