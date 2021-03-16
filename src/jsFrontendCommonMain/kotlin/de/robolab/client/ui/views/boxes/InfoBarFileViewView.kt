package de.robolab.client.ui.views.boxes

import de.robolab.client.app.model.file.details.InfoBarFileView
import de.robolab.client.app.model.file.details.PathDetailBox
import de.robolab.client.app.model.file.details.PlanetStatisticsDetailBox
import de.robolab.client.app.model.file.details.PointDetailBox
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.ui.ViewFactory
import de.robolab.client.utils.runAsync
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.extra.scrollBoxView

class InfoBarFileViewView(
    private val viewModel: InfoBarFileView,
) : ViewCollection<View>() {

    private fun updateContent(box: BoxView) {
        box.clear()

        when (val content = viewModel.detailBoxProperty.value) {
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
            viewModel.uiController.infoBarVisibleProperty.onChange {
                runAsync {
                    updateScrollBox()
                }
            }
            resizeBox(1.0) {
                viewModel.detailBoxProperty.onChange {
                    updateContent(this)
                }
                updateContent(this)
            }
        }
    }

    companion object : ViewFactory {
        override fun matches(viewModel: ViewModel): Boolean {
            return viewModel is InfoBarFileView
        }

        override fun create(viewModel: ViewModel): View {
            return InfoBarFileViewView(viewModel as InfoBarFileView)
        }
    }
}
