package de.robolab.client.ui.views

import de.robolab.client.app.model.file.InfoBarFileView
import de.robolab.client.app.model.file.PathDetailBox
import de.robolab.client.app.model.file.PlanetStatisticsDetailBox
import de.robolab.client.app.model.file.PointDetailBox
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.extra.scrollBoxView

class InfoBarFileViewView(
    private val content: InfoBarFileView
) : ViewCollection<View>() {

    private fun updateContent(box: BoxView) {
        box.clear()

        when (val content = content.detailBoxProperty.value) {
            is PlanetStatisticsDetailBox -> {
                box.add(DetailBoxPlanetStatistics(content))
            }
            is PathDetailBox -> {
                box.add(DetailBoxPath(content))
            }
            is PointDetailBox -> {
                box.add(DetailBoxPoint(content))
            }
        }
    }

    init {
        scrollBoxView {
            resizeBox(1.0) {
                content.detailBoxProperty.onChange {
                    updateContent(this)
                }
                updateContent(this)
            }
        }
    }
}
