package de.robolab.client.ui.views

import de.robolab.client.app.controller.StatusBarController
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.textView

class StatusBar(
    private val statusBarController: StatusBarController
) : ViewCollection<View>() {

    private fun BoxView.updateStatusBar() {
        clear()
        for (item in statusBarController.entryListProperty.value) {
            textView(item)
        }
    }

    init {

        boxView("connection-indicator") {
            classList.bind(
                "success",
                statusBarController.statusColor.mapBinding { it == StatusBarController.StatusColor.SUCCESS })
            classList.bind(
                "warn",
                statusBarController.statusColor.mapBinding { it == StatusBarController.StatusColor.WARN })
            classList.bind(
                "error",
                statusBarController.statusColor.mapBinding { it == StatusBarController.StatusColor.ERROR })

            textView(statusBarController.statusMessage)
            textView(statusBarController.statusActionLabel) {
                onClick {
                    statusBarController.onStatusAction()
                }
            }
        }

        boxView {
            updateStatusBar()
            statusBarController.entryListProperty.onChange {
                updateStatusBar()
            }
        }
    }
}
