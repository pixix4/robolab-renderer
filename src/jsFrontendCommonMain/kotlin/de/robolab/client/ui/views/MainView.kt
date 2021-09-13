package de.robolab.client.ui.views

import de.robolab.client.app.viewmodel.MainViewModel
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.ViewFactoryRegistry
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.extra.listFactory

class MainView(
    viewModel: MainViewModel
) : ViewCollection<View>() {

    init {
        +ViewFactoryRegistry.create(viewModel.toolBar)
        +ViewFactoryRegistry.create(viewModel.leftSideBar) {
            classList += "left-side-bar-view"
        }
        +ViewFactoryRegistry.create(viewModel.rightSideBar) {
            classList += "right-side-bar-view"
        }
        +ViewFactoryRegistry.create(viewModel.statusBar)
        +ViewFactoryRegistry.create(viewModel.content)
        +ViewFactoryRegistry.create(viewModel.terminal)

        boxView("dialog-area") {
            listFactory(viewModel.dialogList, {
                DialogView(it)
            })
        }
    }

    companion object : ViewFactory {
        override fun matches(viewModel: ViewModel) = viewModel is MainViewModel
        override fun create(viewModel: ViewModel) = MainView(viewModel as MainViewModel)
    }
}
