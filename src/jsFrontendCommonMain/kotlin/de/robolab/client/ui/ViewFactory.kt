package de.robolab.client.ui

import de.robolab.client.app.viewmodel.ViewModel
import de.westermann.kwebview.View

interface ViewFactory {

    fun matches(viewModel: ViewModel): Boolean

    fun create(viewModel: ViewModel): View
}
