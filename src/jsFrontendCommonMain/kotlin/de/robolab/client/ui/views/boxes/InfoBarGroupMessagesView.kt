package de.robolab.client.ui.views.boxes

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.group.InfoBarGroupMessages
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.app.viewmodel.buildFormContent
import de.robolab.client.communication.From
import de.robolab.client.communication.RobolabMessage
import de.robolab.client.renderer.events.KeyCode
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.ViewFactoryRegistry
import de.robolab.client.ui.adapter.getKeyCode
import de.robolab.client.ui.views.utils.buttonGroup
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.utils.runAsync
import de.robolab.common.utils.formatDateTime
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*
import de.westermann.kwebview.extra.listFactory
import de.westermann.kwebview.extra.scrollBoxView
import kotlinx.datetime.Instant

class InfoBarGroupMessagesView(
    private val viewModel: InfoBarGroupMessages,
) : ViewCollection<View>() {

    init {
        scrollBoxView {
            viewModel.uiController.infoBarVisibleProperty.onChange {
                runAsync {
                    updateScrollBox()
                }
            }
            resizeBox(0.2) {
                table("info-bar-group-view-header") {
                    row {
                        cell {
                            textView("Messages")
                        }
                        cell {
                            textView(viewModel.messageCountStringProperty)
                        }
                    }

                    row {
                        cell {
                            textView("First message")
                        }
                        cell {
                            textView(viewModel.firstMessageTimeStringProperty)
                        }
                    }

                    row {
                        cell {
                            textView("Last message")
                        }
                        cell {
                            textView(viewModel.lastMessageTimeStringProperty)
                        }
                    }

                    row {
                        cell {
                            textView("Attempt duration")
                        }
                        cell {
                            textView(viewModel.attemptDurationStringProperty)
                        }
                    }
                }

                +ViewFactoryRegistry.create(buildFormContent {
                    button("Send message") {
                        viewModel.openSendDialog()
                    }
                })

                boxView("exam-mode-buttons") {
                    textView("Exam")
                    classList.bind("active", PreferenceStorage.examActiveProperty)

                    val pairList = PreferenceStorage.examPlanets.split(";")

                    if (pairList.isNotEmpty()) {
                        +ViewFactoryRegistry.create(buildFormContent {
                            for (pair in pairList) {
                                if ('=' in pair) {
                                    val (name, planetName) = pair.split("=", limit = 2)
                                    button(name) {
                                        viewModel.openSendDialogExamPlanet(planetName)
                                    }
                                }
                            }
                        })
                    }
                }
            }
            resizeBox(0.5) {
                table("info-bar-group-view-content") {
                    thead {
                        row {
                            head {
                                textView("Time")
                            }
                            head {
                                textView("From")
                            }
                            head {
                                textView("Summary")
                            }
                        }
                    }
                    tbody {
                        listFactory(viewModel.messages, factory = {
                            InfoBarGroupViewCell(it, viewModel, this@resizeBox)
                        })
                    }

                    allowFocus()
                    onKeyDown { event ->
                        when (event.getKeyCode()) {
                            KeyCode.ARROW_UP -> {
                                viewModel.undo()
                                event.preventDefault()
                                event.stopPropagation()
                            }
                            KeyCode.ARROW_DOWN -> {
                                viewModel.redo()
                                event.preventDefault()
                                event.stopPropagation()
                            }
                            else -> {
                            }
                        }
                    }
                }
            }
            resizeBox(0.3) {
                textView(viewModel.headerProperty) {
                    classList += "info-bar-group-view-header-text"
                }
                table("info-bar-group-view-header", "info-bar-group-view-message") {
                    row {
                        cell {
                            textView("From")
                        }
                        cell {
                            boxView("info-bar-group-view-message-from") {
                                textView(viewModel.fromProperty)
                                +generateFromIcon(viewModel.fromEnumProperty)
                            }
                        }
                    }

                    row {
                        cell {
                            textView("Group")
                        }
                        cell {
                            textView(viewModel.groupProperty)
                        }
                    }

                    row {
                        cell {
                            textView("Topic")
                        }
                        cell {
                            textView(viewModel.topicProperty)
                        }
                    }

                    row {
                        cell {
                            textView("Time")
                        }
                        cell {
                            textView(viewModel.timeProperty)
                        }
                    }
                    row {
                        cell {
                            textView("Details")
                        }
                        cell {
                            textView(viewModel.detailsProperty)
                        }
                    }

                    row {
                        cell {
                            colSpan = 2
                            textView(viewModel.rawMessageProperty) {
                                style {
                                    whiteSpace = "pre"
                                    fontFamily = "\"Roboto Mono\""
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    companion object : ViewFactory {
        override fun matches(viewModel: ViewModel): Boolean {
            return viewModel is InfoBarGroupMessages
        }

        override fun create(viewModel: ViewModel): View {
            return InfoBarGroupMessagesView(viewModel as InfoBarGroupMessages)
        }

        private fun fromToIcon(from: From): MaterialIcon = when (from) {
            From.CLIENT -> MaterialIcon.EAST
            From.SERVER -> MaterialIcon.WEST
            From.DEBUG -> MaterialIcon.DNS
            From.UNKNOWN -> MaterialIcon.HELP_OUTLINE
            From.ADMIN -> MaterialIcon.VERIFIED_USER
        }

        private fun fromToClass(from: From): String = when (from) {
            From.CLIENT -> "info-bar-group-client"
            From.SERVER -> "info-bar-group-server"
            From.DEBUG -> "info-bar-group-debug"
            From.UNKNOWN -> "info-bar-group-unknown"
            From.ADMIN -> "info-bar-group-admin"
        }

        fun generateFromIcon(from: From): IconView {
            val iconView = IconView(fromToIcon(from))

            iconView.classList += "info-bar-group-icon"
            iconView.classList += fromToClass(from)
            iconView.title =
                from.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

            return iconView
        }

        fun generateFromIcon(from: ObservableValue<From>): IconView {
            val iconView = generateFromIcon(from.value)

            from.onChange {
                iconView.icon = fromToIcon(from.value)
                for (c in iconView.classList) {
                    if (c != "info-bar-group-icon" && c != "icon-view") {
                        iconView.classList -= c
                    }
                }
                iconView.classList += fromToClass(from.value)
                iconView.title = from.value.name.lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }

            return iconView
        }
    }

    class InfoBarGroupViewCell(
        private val message: RobolabMessage,
        private val content: InfoBarGroupMessages,
        private val scrollView: BoxView,
    ) : TableRow() {

        private val index = content.messages.indexOf(message)
        private val selectedProperty = content.selectedIndexProperty.mapBinding { it == index }

        private fun scrollIntoView2() {
            val viewTop = offsetTopTotal(1)
            val viewBottom = viewTop + clientHeight

            val parentTop = scrollView.scrollTop.toInt()
            val parentBottom = parentTop + scrollView.clientHeight

            val padding = clientHeight * 2

            if (viewTop - padding <= parentTop) {
                scrollView.scrollTo(
                    top = viewTop - padding
                )
            } else if (viewBottom + padding >= parentBottom) {
                scrollView.scrollTo(
                    top = viewBottom + padding - scrollView.clientHeight
                )
            }
        }

        init {
            classList.bind("selected", selectedProperty)

            cell {
                textView(formatDateTime(Instant.fromEpochMilliseconds(message.metadata.time),
                    InfoBarGroupMessages.TIME_FORMAT_DETAILED))
            }
            cell {
                +InfoBarGroupMessagesView.generateFromIcon(message.metadata.from)
            }
            cell {
                textView(message.summary)
            }

            onClick {
                content.selectedIndexProperty.value = index
            }

            selectedProperty.onChange {
                if (selectedProperty.value) {
                    scrollIntoView2()
                }
            }

            if (selectedProperty.value) {
                runAsync { scrollIntoView2() }
            }
        }
    }
}
