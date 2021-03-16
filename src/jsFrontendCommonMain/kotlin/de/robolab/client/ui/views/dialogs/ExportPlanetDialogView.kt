package de.robolab.client.ui.views.dialogs

import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.app.viewmodel.dialog.ExportPlanetDialogViewModel
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.ViewFactoryRegistry
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection

class ExportPlanetDialogView(viewModel: ExportPlanetDialogViewModel) : ViewCollection<View>() {

    init {
        +ViewFactoryRegistry.create(viewModel.content)
    }

    companion object : ViewFactory {
        override fun matches(viewModel: ViewModel): Boolean {
            return viewModel is ExportPlanetDialogViewModel
        }

        override fun create(viewModel: ViewModel): View {
            return ExportPlanetDialogView(viewModel as ExportPlanetDialogViewModel)
        }
    }
}
