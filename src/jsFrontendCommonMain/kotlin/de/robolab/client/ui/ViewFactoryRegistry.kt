package de.robolab.client.ui

import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.common.utils.Logger
import de.westermann.kwebview.View
import de.westermann.kwebview.components.BoxView

object ViewFactoryRegistry {

    private val logger = Logger("ViewFactory")
    private val factoryList = mutableListOf<ViewFactory>()

    fun register(factory: ViewFactory) {
        factoryList += factory
    }

    fun create(viewModel: ViewModel, init: View.() -> Unit = {}): View {
        for (factory in factoryList) {
            if (factory.matches(viewModel)) {
                val view = factory.create(viewModel)
                init(view)
                return view
            }
        }

        logger.error { "View model ${viewModel::class.simpleName} is not supported!" }
        return BoxView()
    }
}
