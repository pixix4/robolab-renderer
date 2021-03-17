package de.robolab.client.ui.views.boxes

import de.robolab.client.app.model.file.details.PathDetailBox
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.ViewFactoryRegistry
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection


class DetailBoxPathView(
    viewModel: PathDetailBox
) : ViewCollection<View>() {

    init {
        +ViewFactoryRegistry.create(viewModel.content)
    }

    companion object : ViewFactory {
        override fun matches(viewModel: ViewModel): Boolean {
            return viewModel is PathDetailBox
        }

        override fun create(viewModel: ViewModel): View {
            return DetailBoxPathView(viewModel as PathDetailBox)
        }
    }
}
