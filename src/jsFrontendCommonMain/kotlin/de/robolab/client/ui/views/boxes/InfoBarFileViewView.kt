package de.robolab.client.ui.views.boxes

import de.robolab.client.app.model.file.details.InfoBarFileView
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.ViewFactoryRegistry
import de.robolab.client.utils.runAsync
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.bindView
import de.westermann.kwebview.extra.scrollBoxView

class InfoBarFileViewView(
    private val viewModel: InfoBarFileView,
) : ViewCollection<View>() {

    init {
        scrollBoxView {
            viewModel.uiController.infoBarVisibleProperty.onChange {
                runAsync {
                    updateScrollBox()
                }
            }
            resizeBox(1.0) {
                bindView(viewModel.detailBoxProperty) {
                    ViewFactoryRegistry.create(it)
                }
            }
        }
    }

    companion object : ViewFactory {
        override fun matches(viewModel: ViewModel): Boolean {
            return viewModel is InfoBarFileView
        }

        override fun create(viewModel: ViewModel): View {
            return InfoBarFileViewView(viewModel as InfoBarFileView)
        }
    }
}
