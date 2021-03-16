package de.robolab.client.ui.views.dialogs

import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.app.viewmodel.dialog.SettingsDialogViewModel
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.ViewFactoryRegistry
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection

class SettingsDialogView(viewModel: SettingsDialogViewModel) : ViewCollection<View>() {

    init {
        +ViewFactoryRegistry.create(viewModel.content)
    }

    companion object : ViewFactory {
        override fun matches(viewModel: ViewModel): Boolean {
            return viewModel is SettingsDialogViewModel
        }

        override fun create(viewModel: ViewModel): View {
            return SettingsDialogView(viewModel as SettingsDialogViewModel)
        }
    }
}
