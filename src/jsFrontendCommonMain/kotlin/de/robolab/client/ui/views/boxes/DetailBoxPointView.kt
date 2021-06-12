package de.robolab.client.ui.views.boxes

import de.robolab.client.app.model.file.boxes.PointDetailBox
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.ViewFactoryRegistry
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection


class DetailBoxPointView(
    viewModel: PointDetailBox
) : ViewCollection<View>() {

    init {
        +ViewFactoryRegistry.create(viewModel.content)
    }

    companion object : ViewFactory {
        override fun matches(viewModel: ViewModel): Boolean {
            return viewModel is PointDetailBox
        }

        override fun create(viewModel: ViewModel): View {
            return DetailBoxPointView(viewModel as PointDetailBox)
        }
    }
}
