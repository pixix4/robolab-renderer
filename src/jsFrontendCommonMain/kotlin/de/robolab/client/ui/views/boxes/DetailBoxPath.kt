package de.robolab.client.ui.views.boxes

import de.robolab.client.app.model.file.details.PathDetailBox
import de.robolab.client.renderer.drawable.utils.radiantToDegree
import de.robolab.client.ui.dialog.bindStringParsing
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*
import kotlin.math.roundToInt


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
                head(2) {
                    textView("Classification")
                }
            }

            row {
                cell {
                    textView("Classifier")
                }
                cell {
                    textView(data.classification?.classifier?.desc ?: "")
                }
            }
            row {
                cell {
                    textView("Difficulty")
                }
                cell {
                    textView(data.classification?.difficulty?.name?.toLowerCase()?.capitalize() ?: "")
                }
            }
            row {
                cell {
                    textView("Score")
                }
                cell {
                    textView(data.classification?.score?.toString() ?: "")
                }
            }
            row {
                cell {
                    textView("Curviness")
                }
                cell {
                    textView(data.classification?.completeSegment?.curviness?.roundToInt()?.toString() ?: "")
                }
            }

            row {
                cell {
                    textView("Details")
                }
                cell {
                    textView(data.classification?.table ?: "") {
                        style {
                            whiteSpace = "pre"
                            fontFamily = "\"Roboto Mono\""
                        }
                    }
                }
            }

            DetailBoxPoint.initList(this, "Path exposed at", data.pathExposedAt)
        }
    }
}
