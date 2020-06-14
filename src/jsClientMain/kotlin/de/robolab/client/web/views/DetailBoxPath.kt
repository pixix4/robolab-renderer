package de.robolab.client.web.views

import de.robolab.client.app.model.file.PathDetailBox
import de.robolab.client.web.dialog.bindStringParsing
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*


class DetailBoxPath(
    private val data: PathDetailBox
) : ViewCollection<View>() {

    init {
        table("info-bar-group-view-header") {
            row {
                head(2) {
                    textView("Path")
                }
            }

            row {
                cell {
                    textView("Source")
                }
                cell {
                    textView(data.source)
                }
            }

            row {
                cell {
                    textView("Target")
                }
                cell {
                    textView(data.target)
                }
            }

            row {
                cell {
                    textView("Weight")
                }
                cell {
                    label(inputView(data.weightProperty.bindStringParsing()) {
                        readonly = true
                    })
                }
            }

            row {
                cell {
                    textView("Hidden")
                }
                cell {
                    checkbox(data.isHiddenProperty)
                }
            }

            row {
                cell {
                    textView("Length")
                }
                cell {
                    textView(data.length)
                }
            }

            row {
                cell {
                    textView("Classifier")
                }
                cell {
                    textView(data.classifier)
                }
            }

            DetailBoxPoint.initList(this, "Path exposed at", data.pathExposedAt)
        }
    }
}
