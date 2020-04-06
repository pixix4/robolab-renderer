package de.robolab.web.views

import de.robolab.app.controller.StatusBarController
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.TextView
import de.westermann.kwebview.components.textView
import de.westermann.kwebview.extra.listFactory

class StatusBar(private val statusBarController: StatusBarController) : ViewCollection<View>() {

    private fun updateStatusBar() {
        clear()
        for (item in statusBarController.entryListProperty.value) {
            textView(item)
        }
    }

    init {
        updateStatusBar()
        statusBarController.entryListProperty.onChange {
            updateStatusBar()
        }
    }
}
