package de.robolab.client.ui.views.boxes

import de.robolab.client.app.model.file.details.PlanetStatisticsDetailBox
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.ViewFactoryRegistry
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection


class DetailBoxPlanetStatisticsView(
    viewModel: PlanetStatisticsDetailBox
) : ViewCollection<View>() {

    init {
        +ViewFactoryRegistry.create(viewModel.content)
    }

    companion object : ViewFactory {
        override fun matches(viewModel: ViewModel): Boolean {
            return viewModel is PlanetStatisticsDetailBox
        }

        override fun create(viewModel: ViewModel): View {
            return DetailBoxPlanetStatisticsView(viewModel as PlanetStatisticsDetailBox)
        }
    }
}
