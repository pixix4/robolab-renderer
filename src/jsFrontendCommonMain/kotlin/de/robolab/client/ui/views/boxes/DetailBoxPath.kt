package de.robolab.client.ui.views.boxes

import de.robolab.client.app.model.file.details.PathDetailBox
import de.robolab.client.ui.dialog.bindStringParsing
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
                    inputView(data.weightProperty.bindStringParsing())
                }
            }

            row {
                cell {
                    textView("Hidden")
                }
                cell {
                    label(checkbox(data.isHiddenProperty))
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
