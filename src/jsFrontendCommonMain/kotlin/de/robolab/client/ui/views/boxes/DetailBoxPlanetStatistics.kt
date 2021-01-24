package de.robolab.client.ui.views.boxes

import de.robolab.client.app.model.file.details.PlanetStatisticsDetailBox
import de.westermann.kobserve.event.now
import de.westermann.kobserve.property.mapBinding
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.*


class DetailBoxPlanetStatistics(
    private val data: PlanetStatisticsDetailBox
) : ViewCollection<View>() {

    init {
        table("info-bar-group-view-header") {
            for ((label, block) in data.data) {
                tbody {
                    val refs = mutableListOf<TableRow>()

                    block.onChange.now {
                        clear()
                        for (r in refs) {
                            r.classList.unbind("empty")
                        }
                        refs.clear()

                        if (block.value.isNotEmpty()) {
                            row {
                                head(2) {
                                    textView(label)
                                }
                            }

                            for ((key, value) in block.value) {
                                refs += row {
                                    classList.bind("empty", value.mapBinding { it.isBlank() })
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
        }
    }
}
