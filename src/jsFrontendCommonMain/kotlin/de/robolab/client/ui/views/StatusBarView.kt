package de.robolab.client.ui.views

import de.robolab.client.app.controller.ConnectionController
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.viewmodel.StatusBarViewModel
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.ui.ViewFactory
import de.westermann.kobserve.event.now
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.bindView
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.iconView
import de.westermann.kwebview.components.textView
import de.westermann.kwebview.extra.listFactory

class StatusBarView(
    private val viewModel: StatusBarViewModel
) : ViewCollection<View>() {

    init {
        boxView {
            listFactory(viewModel.connectionList, { connection ->
                BoxView().apply {
                    classList += "connection-indicator"

                    classList.bind(
                        "success",
                        connection.statusColor.mapBinding { it == ConnectionController.StatusColor.SUCCESS })
                    classList.bind(
                        "warn",
                        connection.statusColor.mapBinding { it == ConnectionController.StatusColor.WARN })
                    classList.bind(
                        "error",
                        connection.statusColor.mapBinding { it == ConnectionController.StatusColor.ERROR })
                    classList.bind(
                        "other",
                        connection.statusColor.mapBinding { it == ConnectionController.StatusColor.OTHER })

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
            viewModel.contentList.onChange.now {
                clear()
                for (item in viewModel.contentList.value) {
                    textView(item)
                }
            }
        }

        boxView {
            listFactory(viewModel.progressList, { progress ->
                BoxView().apply {
                    classList += "progress-indicator"

                    textView(progress.name)
                    textView(progress.labelProperty)
                }
            })
        }

        boxView("status-bar-spacer")

        boxView("status-bar-fullscreen-close") {
            bindView(viewModel.fullscreenProperty) {
                if (it) {
                    iconView(MaterialIcon.FULLSCREEN_EXIT) {
                        onClick {
                            viewModel.fullscreenProperty.value = false
                        }
                    }
                } else {
                    BoxView()
                }
            }
        }
    }

    companion object : ViewFactory {

        override fun matches(viewModel: ViewModel): Boolean {
            return viewModel is StatusBarViewModel
        }

        override fun create(viewModel: ViewModel): View {
            return StatusBarView(viewModel as StatusBarViewModel)
        }
    }
}
