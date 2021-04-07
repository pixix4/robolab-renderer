package de.robolab.client.ui.views.dialogs

import de.robolab.client.app.viewmodel.FormViewModel
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.app.viewmodel.dialog.SettingsDialogViewModel
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.ViewFactoryRegistry
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.property
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.bindView
import de.westermann.kwebview.components.BoxView
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.textView
import de.westermann.kwebview.extra.listFactory

class SettingsDialogView(viewModel: SettingsDialogViewModel) : ViewCollection<View>() {

    private val selectedIndexProperty = property(0)
    private var selectedIndex by selectedIndexProperty

    init {
        boxView("settings-dialog-header") {
            boxView("tab-bar-view") {
                listFactory(viewModel.content.contentProperty, { formViewModel ->
                    if (formViewModel is FormViewModel.LabeledGroup) {
                        BoxView().apply {
                            classList += "tab-bar-view-item"
                            classList += "tab-bar-view-item-labeled"
                            classList.bind("active", selectedIndexProperty.mapBinding {
                                it == viewModel.content.contentProperty.indexOf(formViewModel)
                            })

                            textView(formViewModel.labelProperty) {
                                classList += "tab-bar-view-label"
                            }

                            onClick {
                                selectedIndex = viewModel.content.contentProperty.indexOf(formViewModel)
                            }
                        }
                    } else {
                        BoxView()
                    }
                })
            }
        }
        boxView("settings-dialog-body") {
            bindView(selectedIndexProperty) {
                val formViewModel = viewModel.content.contentProperty[it] as FormViewModel.LabeledGroup
                ViewFactoryRegistry.create(FormViewModel.Group(formViewModel.contentProperty))
            }
        }
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
