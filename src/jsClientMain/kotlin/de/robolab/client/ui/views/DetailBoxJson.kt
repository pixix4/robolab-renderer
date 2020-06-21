package de.robolab.client.ui.views

import de.robolab.client.app.model.group.JsonDetailBox
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.cell
import de.westermann.kwebview.components.row
import de.westermann.kwebview.components.table
import de.westermann.kwebview.components.textView


class DetailBoxJson(
    private val data: JsonDetailBox
) : ViewCollection<View>() {

    init {
        textView(data.header)
        table("info-bar-group-view-header") {
            row {
                cell {
                    textView("From")
                }
                cell {
                    textView(data.from)
                }
            }

            row {
                cell {
                    textView("Group")
                }
                cell {
                    textView(data.group)
                }
            }

            row {
                cell {
                    textView("Topic")
                }
                cell {
                    textView(data.topic)
                }
            }

            row {
                cell {
                    textView("Time")
                }
                cell {
                    textView(data.time)
                }
            }
            row {
                cell {
                    textView("Details")
                }
                cell {
                    textView(data.details)
                }
            }

            row {
                cell {
                    textView("Message")
                }
                cell {
                    textView(data.rawMessage) {
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
