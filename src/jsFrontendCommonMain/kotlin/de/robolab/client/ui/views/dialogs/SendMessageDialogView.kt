package de.robolab.client.ui.views.dialogs

import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.app.viewmodel.dialog.SendMessageDialogViewModel
import de.robolab.client.app.viewmodel.dialog.SettingsDialogViewModel
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.ViewFactoryRegistry
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection

class SendMessageDialogView(viewModel: SendMessageDialogViewModel) : ViewCollection<View>() {

    init {
        +ViewFactoryRegistry.create(viewModel.content)
    }

    companion object : ViewFactory {
        override fun matches(viewModel: ViewModel): Boolean {
            return viewModel is SendMessageDialogViewModel
        }

        override fun create(viewModel: ViewModel): View {
            return SendMessageDialogView(viewModel as SendMessageDialogViewModel)
        }
    }
}
