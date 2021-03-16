package de.robolab.client.ui.views

import de.robolab.client.app.viewmodel.ToolBarViewModel
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.views.utils.FormContentView
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.textView
import de.westermann.kwebview.extra.listFactory

class ToolBarView(
    private val viewModel: ToolBarViewModel
) : ViewCollection<View>() {


    init {
        boxView("tool-bar-left") {
            listFactory(viewModel.leftActionList, {
                FormContentView.createView(it)
            })
        }

        textView(viewModel.titleProperty) {
            classList += "tool-bar-center"
        }

        boxView("tool-bar-right") {
            listFactory(viewModel.rightActionList, {
                FormContentView.createView(it)
            })
        }
    }

    companion object : ViewFactory {
        override fun matches(viewModel: ViewModel) = viewModel is ToolBarViewModel
        override fun create(viewModel: ViewModel) = ToolBarView(viewModel as ToolBarViewModel)
    }
}
