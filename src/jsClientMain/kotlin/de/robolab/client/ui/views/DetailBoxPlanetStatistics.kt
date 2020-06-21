package de.robolab.client.ui.views

import de.robolab.client.app.model.file.PlanetStatisticsDetailBox
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*


class DetailBoxPlanetStatistics(
    private val data: PlanetStatisticsDetailBox
) : ViewCollection<View>() {

    init {
        table("info-bar-group-view-header") {
            for ((label, block) in data.data) {
                row {
                    head(2) {
                        textView(label)
                    }
                }

                for ((key, value) in block) {
                    row {
                        cell {
                            textView(key)
                        }
                        cell {
                            textView(value)
                        }
                    }
                }
            }
        }
    }
}
