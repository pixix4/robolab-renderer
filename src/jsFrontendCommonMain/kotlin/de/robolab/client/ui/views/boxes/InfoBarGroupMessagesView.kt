package de.robolab.client.ui.views.boxes

import com.soywiz.klock.format
import de.robolab.client.app.controller.UiController
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.group.InfoBarGroupMessages
import de.robolab.client.communication.From
import de.robolab.client.communication.RobolabMessage
import de.robolab.client.renderer.events.KeyCode
import de.robolab.client.utils.runAsync
import de.robolab.client.ui.adapter.toCommon
import de.robolab.client.ui.views.utils.buttonGroup
import de.robolab.client.utils.PreferenceStorage
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*
import de.westermann.kwebview.extra.listFactory
import de.westermann.kwebview.extra.scrollBoxView

class InfoBarGroupMessagesView(
    private val content: InfoBarGroupMessages,
    private val uiController: UiController
) : ViewCollection<View>() {

    init {
        scrollBoxView {
            uiController.infoBarVisibleProperty.onChange {
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
                            textView(content.messageCountStringProperty)
                        }
                    }

                    row {
                        cell {
                            textView("First message")
                        }
                        cell {
                            textView(content.firstMessageTimeStringProperty)
                        }
                    }

                    row {
                        cell {
                            textView("Last message")
                        }
                        cell {
                            textView(content.lastMessageTimeStringProperty)
                        }
                    }

                    row {
                        cell {
                            textView("Attempt duration")
                        }
                        cell {
                            textView(content.attemptDurationStringProperty)
                        }
                    }
                }

                buttonGroup(true) {
                    button("Send message") {
                        onClick {
                            content.openSendDialog()
                        }
                    }
                }

                boxView("exam-mode-buttons") {
                    textView("Exam")
                    classList.bind("active", PreferenceStorage.examActiveProperty)

                    buttonGroup {
                        button("Small Planet") {
                            onClick {
                                content.openSendDialogSmallExamPlanet()
                            }
                        }

                        button("Large Planet") {
                            onClick {
                                content.openSendDialogLargeExamPlanet()
                            }
                        }
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
                        listFactory(content.messages, factory = {
                            InfoBarGroupViewCell(it, content, this@resizeBox)
                        })
                    }

                    allowFocus()
                    onKeyDown { event ->
                        when (event.toCommon()) {
                            KeyCode.ARROW_UP -> {
                                content.undo()
                                event.preventDefault()
                                event.stopPropagation()
                            }
                            KeyCode.ARROW_DOWN -> {
                                content.redo()
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
                textView(content.headerProperty) {
                    classList += "info-bar-group-view-header-text"
                }
                table("info-bar-group-view-header", "info-bar-group-view-message") {
                    row {
                        cell {
                            textView("From")
                        }
                        cell {
                            boxView("info-bar-group-view-message-from") {
                                textView(content.fromProperty)
                                +generateFromIcon(content.fromEnumProperty)
                            }
                        }
                    }

                    row {
                        cell {
                            textView("Group")
                        }
                        cell {
                            textView(content.groupProperty)
                        }
                    }

                    row {
                        cell {
                            textView("Topic")
                        }
                        cell {
                            textView(content.topicProperty)
                        }
                    }

                    row {
                        cell {
                            textView("Time")
                        }
                        cell {
                            textView(content.timeProperty)
                        }
                    }
                    row {
                        cell {
                            textView("Details")
                        }
                        cell {
                            textView(content.detailsProperty)
                        }
                    }

                    row {
                        cell {
                            colSpan = 2
                            textView(content.rawMessageProperty) {
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

    companion object {
        private fun fromToIcon(from: From): MaterialIcon = when(from) {
                From.CLIENT -> MaterialIcon.WEST
                From.SERVER -> MaterialIcon.EAST
                From.DEBUG -> MaterialIcon.DNS
                From.UNKNOWN -> MaterialIcon.HELP_OUTLINE
        }

        private fun fromToClass(from: From): String = when(from) {
                From.CLIENT ->  "info-bar-group-client"
                From.SERVER ->  "info-bar-group-server"
                From.DEBUG ->  "info-bar-group-debug"
                From.UNKNOWN ->  "info-bar-group-unknown"
        }
        fun generateFromIcon(from: From): IconView {
            val iconView = IconView(fromToIcon(from))

            iconView.classList += "info-bar-group-icon"
            iconView.classList += fromToClass(from)
            iconView.title = from.name.toLowerCase().capitalize()

            return iconView
        }
        fun generateFromIcon(from: ObservableValue<From>): IconView {
            val iconView = generateFromIcon(from.value)

            from.onChange {
                iconView.icon = fromToIcon(from.value)
                for (c in iconView.classList) {
                    if (c !=  "info-bar-group-icon" && c != "icon-view") {
                        iconView.classList -= c
                    }
                }
                iconView.classList += fromToClass(from.value)
                iconView.title = from.value.name.toLowerCase().capitalize()
            }

            return iconView
        }
    }
}

class InfoBarGroupViewCell(
    private val message: RobolabMessage,
    private val content: InfoBarGroupMessages,
    private val scrollView: BoxView
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
            textView(InfoBarGroupMessages.TIME_FORMAT_DETAILED.format(message.metadata.time))
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
