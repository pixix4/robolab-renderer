package de.robolab.client.ui.views

import de.robolab.client.app.controller.StatusBarController
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.textView
import de.westermann.kwebview.extra.listFactory

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
        boxView {
            listFactory(statusBarController.connectionIndicatorList, { connection ->
                BoxView().apply {
                    classList += "connection-indicator"

                    classList.bind(
                        "success",
                        connection.statusColor.mapBinding { it == StatusBarController.StatusColor.SUCCESS })
                    classList.bind(
                        "warn",
                        connection.statusColor.mapBinding { it == StatusBarController.StatusColor.WARN })
                    classList.bind(
                        "error",
                        connection.statusColor.mapBinding { it == StatusBarController.StatusColor.ERROR })
                    classList.bind(
                        "other",
                        connection.statusColor.mapBinding { it == StatusBarController.StatusColor.OTHER })

                    textView(connection.name)

                    textView(connection.statusLabel)

                    textView(connection.actionLabel) {
                        onClick {
                            connection.actionHandler()
                        }
                    }
                }
            })
        }

        boxView {
            updateStatusBar()
            statusBarController.entryListProperty.onChange {
                updateStatusBar()
            }
        }

        boxView {
            textView(statusBarController.memoryUsageProperty)
        }
    }
}
