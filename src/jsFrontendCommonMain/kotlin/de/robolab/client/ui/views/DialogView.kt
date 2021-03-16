package de.robolab.client.ui.views

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.viewmodel.DialogViewModel
import de.robolab.client.ui.ViewFactoryRegistry
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.button
import de.westermann.kwebview.components.iconView
import de.westermann.kwebview.components.textView

class DialogView(private val viewModel: DialogViewModel) : ViewCollection<View>() {

    init {
        boxView("dialog-header") {
            textView(viewModel.title)

            button {
                iconView(MaterialIcon.CLOSE)
                onClick {
                    viewModel.close()
                }
            }
        }

        boxView("dialog-body") {
            +ViewFactoryRegistry.create(viewModel)
        }
    }
}
