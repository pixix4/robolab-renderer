package de.robolab.client.web.views

import de.robolab.client.app.model.file.PointDetailBox
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*


class DetailBoxPoint(
    private val data: PointDetailBox
) : ViewCollection<View>() {

    init {
        table("info-bar-group-view-header") {
            row {
                head(2) {
                    textView("Point")
                }
            }

            row {
                cell {
                    textView("Position")
                }
                cell {
                    textView(data.position)
                }
            }

            row {
                cell {
                    textView("Hidden")
                }
                cell {
                    label(checkbox(data.isHidden) {
                        readonly = true
                    })
                }
            }

            initList(this,"Targets send", data.targetsSend)
            initList(this,"Target exposed at", data.targetExposedAt)
            initList(this,"Path send", data.pathSend)
        }
    }

    companion object {

        fun initList(table: Table, label: String, list: List<String>) {
            if (list.isNotEmpty()) {
                table.row {
                    head(2) {
                        textView(label + list.size.let { if (it > 1) " ($it)" else "" })
                    }
                }

                for (str in list) {
                    table.row {
                        cell(2) {
                            textView(str)
                        }
                    }
                }
            }
        }
    }
}
