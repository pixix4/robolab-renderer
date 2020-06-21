package de.robolab.client.ui.views

import com.soywiz.klock.format
import de.robolab.client.app.model.group.InfoBarGroupInfo
import de.robolab.client.communication.RobolabMessage
import de.robolab.client.renderer.events.KeyCode
import de.robolab.client.utils.runAsync
import de.robolab.client.ui.adapter.toCommon
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*
import de.westermann.kwebview.extra.listFactory

class InfoBarGroupView(
    private val content: InfoBarGroupInfo,
    private val scrollView: BoxView
) : ViewCollection<View>() {

    init {
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
                    InfoBarGroupViewCell(it, content, scrollView)
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
}

class InfoBarGroupViewCell(
    private val message: RobolabMessage,
    private val content: InfoBarGroupInfo,
    private val scrollView: BoxView
) : TableRow() {

    private val index = content.messages.indexOf(message)
    private val selectedProperty = content.selectedIndexProperty.mapBinding { it == index }

    private fun scrollIntoView() {
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
            textView(InfoBarGroupInfo.TIME_FORMAT_DETAILED.format(message.metadata.time))
        }
        cell {
            textView(message.metadata.from.name.toLowerCase().capitalize())
        }
        cell {
            textView(message.summary)
        }

        onClick {
            content.selectedIndexProperty.value = index
        }

        selectedProperty.onChange {
            if (selectedProperty.value) {
                scrollIntoView()
            }
        }

        if (selectedProperty.value) {
            runAsync { scrollIntoView() }
        }
    }
}
