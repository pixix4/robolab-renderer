package de.robolab.client.ui.views.utils

import de.robolab.client.app.viewmodel.FormViewModel
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.ui.ViewFactory
import de.westermann.kobserve.event.now
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.components.boxView
import de.westermann.kwebview.components.textView
import de.westermann.kwebview.extra.listFactory

sealed class FormView(
    open val viewModel: FormViewModel
) : ViewCollection<View>() {


    class FormEntryView(override val viewModel: FormViewModel.Entry) : FormView(viewModel) {

        init {
            viewModel.contentProperty.onChange.now {
                val content = viewModel.contentProperty.value
                clear()
                +FormContentView.createView(content)
            }
        }
    }

    class FormLabeledEntryView(override val viewModel: FormViewModel.LabeledEntry) : FormView(viewModel) {

        init {
            textView(viewModel.labelProperty)
            boxView {
                viewModel.contentProperty.onChange.now {
                    val content = viewModel.contentProperty.value
                    clear()
                    +FormContentView.createView(content)
                }
            }
        }
    }

    class FormGroupView(override val viewModel: FormViewModel.Group) : FormView(viewModel) {

        init {
            listFactory(viewModel.contentProperty, {
                createView(it)
            })
        }
    }

    class FormLabeledGroupView(override val viewModel: FormViewModel.LabeledGroup) : FormView(viewModel) {

        init {
            textView(viewModel.labelProperty)
            boxView {
                listFactory(viewModel.contentProperty, {
                    createView(it)
                })
            }
        }
    }

    companion object : ViewFactory {

        fun createView(viewModel: FormViewModel): View {
            val view = when (viewModel) {
                is FormViewModel.Entry -> FormEntryView(viewModel)
                is FormViewModel.LabeledEntry -> FormLabeledEntryView(viewModel)
                is FormViewModel.Group -> FormGroupView(viewModel)
                is FormViewModel.LabeledGroup -> FormLabeledGroupView(viewModel)
            }
            view.classList += "form-view"
            return view
        }

        override fun matches(viewModel: ViewModel): Boolean {
            return viewModel is FormViewModel
        }

        override fun create(viewModel: ViewModel): View {
            return createView(viewModel as FormViewModel)
        }
    }
}
