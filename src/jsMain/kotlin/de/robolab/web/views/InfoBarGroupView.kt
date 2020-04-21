package de.robolab.web.views

import com.soywiz.klock.format
import de.robolab.app.model.group.InfoBarGroupInfo
import de.robolab.communication.RobolabMessage
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*
import de.westermann.kwebview.extra.listFactory

class InfoBarGroupView(private val content: InfoBarGroupInfo): ViewCollection<View>() {

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
                    InfoBarGroupViewCell(it, content)
                })
            }
        }
    }
}

class InfoBarGroupViewCell(private val message: RobolabMessage, private val content: InfoBarGroupInfo): TableRow() {

    private val index = content.messages.indexOf(message)
    private val selectedProperty = content.selectedIndexProperty.mapBinding { it == index }

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
    }
}
