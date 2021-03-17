package de.robolab.client.ui.views.boxes

import de.robolab.client.app.model.file.details.InfoBarFilePaper
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.ViewFactoryRegistry
import de.robolab.client.utils.runAsync
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.bindView
import de.westermann.kwebview.extra.scrollBoxView

class InfoBarFilePaperView(
    private val viewModel: InfoBarFilePaper,
) : ViewCollection<View>() {

    init {
        scrollBoxView {
            viewModel.uiController.infoBarVisibleProperty.onChange {
                runAsync {
                    updateScrollBox()
                }
            }
            resizeBox(0.5) {
                +ViewFactoryRegistry.create(viewModel.topContent)
            }
            resizeBox(0.5) {
                bindView(viewModel.detailBoxProperty) {
                    ViewFactoryRegistry.create(it)
                }
            }
        }
    }

    companion object : ViewFactory {
        override fun matches(viewModel: ViewModel): Boolean {
            return viewModel is InfoBarFilePaper
        }

        override fun create(viewModel: ViewModel): View {
            return InfoBarFilePaperView(viewModel as InfoBarFilePaper)
        }
    }
}
