package de.robolab.client.ui.views.utils

import de.robolab.client.app.viewmodel.FormButtonContentViewModel
import de.robolab.client.app.viewmodel.FormContentViewModel
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.renderer.events.KeyCode
import de.robolab.client.ui.ViewFactory
import de.robolab.client.ui.adapter.toEvent
import de.robolab.client.ui.dialog.bindStringParsing
import de.robolab.common.utils.Dimension
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.now
import de.westermann.kobserve.not
import de.westermann.kwebview.View
import de.westermann.kwebview.ViewCollection
import de.westermann.kwebview.clientPosition
import de.westermann.kwebview.components.*
import de.westermann.kwebview.extra.listFactory
import kotlinx.browser.window

sealed class FormContentView(
    open val viewModel: FormContentViewModel
) : ViewCollection<View>() {

    class FormContentStringInputView(override val viewModel: FormContentViewModel.StringInput) :
        FormContentView(viewModel) {

        init {
            val inputType = when (viewModel.typeHint) {
                FormContentViewModel.InputTypeHint.TEXT -> InputType.TEXT
                FormContentViewModel.InputTypeHint.URL -> InputType.URL
                FormContentViewModel.InputTypeHint.PASSWORD -> InputType.PASSWORD
                FormContentViewModel.InputTypeHint.SEARCH -> InputType.SEARCH
            }
            inputView(inputType, viewModel.valueProperty) {
                disabledProperty.bind(!viewModel.enabledProperty)
                titleProperty.bind(viewModel.descriptionProperty)

                readonly = viewModel.valueProperty !is ObservableProperty

                onKeyDown {
                    val keyCode = it.toEvent().keyCode
                    if (keyCode == KeyCode.ENTER) {
                        viewModel.onSubmit.emit(value)
                    }
                }
            }
        }
    }

    class FormContentIntInputView(override val viewModel: FormContentViewModel.IntInput) : FormContentView(viewModel) {

        init {
            inputView(InputType.NUMBER, viewModel.valueProperty.bindStringParsing()) {
                disabledProperty.bind(!viewModel.enabledProperty)
                titleProperty.bind(viewModel.descriptionProperty)

                viewModel.rangeProperty.onChange.now {
                    val range = viewModel.rangeProperty.value
                    min = range.first.toDouble()
                    max = range.last.toDouble()
                    step = range.step.toDouble()
                }

                readonly = viewModel.valueProperty !is ObservableProperty
            }
        }
    }

    class FormContentDoubleInputView(override val viewModel: FormContentViewModel.DoubleInput) :
        FormContentView(viewModel) {

        init {
            inputView(InputType.NUMBER, viewModel.valueProperty.bindStringParsing()) {
                disabledProperty.bind(!viewModel.enabledProperty)
                titleProperty.bind(viewModel.descriptionProperty)

                viewModel.rangeProperty.onChange.now {
                    val range = viewModel.rangeProperty.value
                    min = range.start
                    max = range.endInclusive
                }
                viewModel.stepProperty.onChange.now {
                    step = viewModel.stepProperty.value
                }

                readonly = viewModel.valueProperty !is ObservableProperty
            }
        }
    }

    class FormContentSelectInputView(override val viewModel: FormContentViewModel.SelectInput) :
        FormContentView(viewModel) {

        init {
            selectView<String>(
                viewModel.optionListProperty.toList(),
                viewModel.valueProperty,
            ) {
                titleProperty.bind(viewModel.descriptionProperty)
                disabledProperty.bind(!viewModel.enabledProperty)

                viewModel.optionListProperty.onChange {
                    dataSet = viewModel.optionListProperty.toList()
                }

                readonly = viewModel.valueProperty !is ObservableProperty
            }
        }
    }

    class FormContentEnumInputView(override val viewModel: FormContentViewModel.EnumInput<*>) :
        FormContentView(viewModel) {

        init {
            selectView<String>(
                viewModel.stringOptionList,
                viewModel.stringValueProperty,
            ) {
                titleProperty.bind(viewModel.descriptionProperty)
                disabledProperty.bind(!viewModel.enabledProperty)

                readonly = viewModel.valueProperty !is ObservableProperty
            }
        }
    }

    class FormContentBooleanInputView(override val viewModel: FormContentViewModel.BooleanInput) :
        FormContentView(viewModel) {

        init {
            label(
                checkbox(viewModel.valueProperty) {
                    disabledProperty.bind(!viewModel.enabledProperty)

                    readonly = viewModel.valueProperty !is ObservableProperty
                }
            ) {
                titleProperty.bind(viewModel.descriptionProperty)
            }
        }
    }

    class FormContentButtonView(override val viewModel: FormContentViewModel.Button) : FormContentView(viewModel) {

        init {
            button {
                bindFormContent(viewModel.contentProperty)

                titleProperty.bind(viewModel.descriptionProperty)
                disabledProperty.bind(!viewModel.enabledProperty)
                onClick { event ->
                    viewModel.onClick(
                        FormContentViewModel.ClickEvent(
                            event.clientPosition,
                            Dimension(window.innerWidth.toDouble(), window.innerHeight.toDouble()),
                            event.ctrlKey || event.metaKey,
                            event.altKey,
                            event.shiftKey
                        )
                    )
                }
            }
        }
    }

    class FormContentToggleButtonView(override val viewModel: FormContentViewModel.ToggleButton) :
        FormContentView(viewModel) {

        init {
            button {
                bindFormContent(viewModel.contentProperty)

                classList.bind("active", viewModel.valueProperty)
                titleProperty.bind(viewModel.descriptionProperty)
                disabledProperty.bind(!viewModel.enabledProperty)
                onClick { event ->
                    viewModel.onClick(
                        FormContentViewModel.ClickEvent(
                            event.clientPosition,
                            Dimension(window.innerWidth.toDouble(), window.innerHeight.toDouble()),
                            event.ctrlKey || event.metaKey,
                            event.altKey,
                            event.shiftKey
                        )
                    )
                }
            }
        }
    }

    class FormContentLabelView(override val viewModel: FormContentViewModel.Label) : FormContentView(viewModel) {

        init {
            textView(viewModel.contentProperty) {
                titleProperty.bind(viewModel.descriptionProperty)
            }
        }
    }

    class FormContentGroupView(override val viewModel: FormContentViewModel.Group) : FormContentView(viewModel) {

        init {
            listFactory(viewModel.contentProperty, {
                createView(it)
            })
        }
    }

    companion object : ViewFactory {

        fun ViewCollection<View>.bindFormContent(observable: ObservableValue<FormButtonContentViewModel>) {
            observable.onChange.now {
                val content = observable.value

                clear()
                when (content) {
                    is FormButtonContentViewModel.Icon -> iconView(content.icon)
                    is FormButtonContentViewModel.Text -> textView(content.text)
                }
            }
        }

        fun createView(viewModel: FormContentViewModel): View {
            val view = when (viewModel) {
                is FormContentViewModel.StringInput -> FormContentStringInputView(viewModel)
                is FormContentViewModel.IntInput -> FormContentIntInputView(viewModel)
                is FormContentViewModel.DoubleInput -> FormContentDoubleInputView(viewModel)
                is FormContentViewModel.SelectInput -> FormContentSelectInputView(viewModel)
                is FormContentViewModel.EnumInput<*> -> FormContentEnumInputView(viewModel)
                is FormContentViewModel.BooleanInput -> FormContentBooleanInputView(viewModel)
                is FormContentViewModel.Button -> FormContentButtonView(viewModel)
                is FormContentViewModel.ToggleButton -> FormContentToggleButtonView(viewModel)
                is FormContentViewModel.Label -> FormContentLabelView(viewModel)
                is FormContentViewModel.Group -> FormContentGroupView(viewModel)
            }
            view.classList += "form-content-view"
            return view
        }

        override fun matches(viewModel: ViewModel): Boolean {
            return viewModel is FormContentViewModel
        }

        override fun create(viewModel: ViewModel): View {
            return createView(viewModel as FormContentViewModel)
        }
    }
}
