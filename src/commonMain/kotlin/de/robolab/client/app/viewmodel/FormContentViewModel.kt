package de.robolab.client.app.viewmodel

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Vector
import de.westermann.kobserve.base.ObservableList
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.*
import de.westermann.kobserve.toggle

sealed class FormContentViewModel: ViewModel {

    class StringInput(
        val valueProperty: ObservableValue<String>,
        val descriptionProperty: ObservableValue<String>,
        val typeHint: InputTypeHint,
        val enabledProperty: ObservableValue<Boolean>,
    ) : FormContentViewModel() {

        val onSubmit = EventHandler<String>()

        constructor(
            valueProperty: ObservableValue<String>,
            description: String = "",
            typeHint: InputTypeHint = InputTypeHint.TEXT,
            enabledProperty: ObservableValue<Boolean> = constObservable(true),
        ) : this(
            valueProperty,
            description.observeConst(),
            typeHint,
            enabledProperty,
        )
    }

    class IntInput(
        val valueProperty: ObservableValue<Int>,
        val rangeProperty: ObservableValue<IntRange>,
        val descriptionProperty: ObservableValue<String>,
        val enabledProperty: ObservableValue<Boolean>,
    ) : FormContentViewModel() {
        constructor(
            valueProperty: ObservableValue<Int>,
            range: IntRange,
            description: String = "",
            enabledProperty: ObservableValue<Boolean> = constObservable(true),
        ) : this(
            valueProperty,
            range.observeConst(),
            description.observeConst(),
            enabledProperty,
        )
    }

    class LongInput(
        val valueProperty: ObservableValue<Long>,
        val rangeProperty: ObservableValue<LongRange>,
        val descriptionProperty: ObservableValue<String>,
        val enabledProperty: ObservableValue<Boolean>,
    ) : FormContentViewModel() {
        constructor(
            valueProperty: ObservableValue<Long>,
            range: LongRange,
            description: String = "",
            enabledProperty: ObservableValue<Boolean> = constObservable(true),
        ) : this(
            valueProperty,
            range.observeConst(),
            description.observeConst(),
            enabledProperty,
        )
    }

    class DoubleInput(
        val valueProperty: ObservableValue<Double>,
        val rangeProperty: ObservableValue<ClosedFloatingPointRange<Double>>,
        val stepProperty: ObservableValue<Double>,
        val descriptionProperty: ObservableValue<String>,
        val enabledProperty: ObservableValue<Boolean>,
    ) : FormContentViewModel() {
        constructor(
            valueProperty: ObservableValue<Double>,
            range: ClosedFloatingPointRange<Double>,
            step: Double,
            description: String = "",
            enabledProperty: ObservableValue<Boolean> = constObservable(true),
        ) : this(
            valueProperty,
            range.observeConst(),
            step.observeConst(),
            description.observeConst(),
            enabledProperty,
        )
    }

    class SelectInput(
        val valueProperty: ObservableValue<String>,
        val optionListProperty: ObservableList<String>,
        val descriptionProperty: ObservableValue<String>,
        val enabledProperty: ObservableValue<Boolean>,
    ) : FormContentViewModel() {
        constructor(
            valueProperty: ObservableValue<String>,
            optionList: List<String>,
            description: String = "",
            enabledProperty: ObservableValue<Boolean> = constObservable(true),
        ) : this(
            valueProperty,
            observableListOf(*optionList.toTypedArray()),
            description.observeConst(),
            enabledProperty,
        )
    }

    class EnumInput<T : Enum<T>>(
        val valueProperty: ObservableValue<T>,
        val optionList: List<T>,
        val descriptionProperty: ObservableValue<String>,
        val enabledProperty: ObservableValue<Boolean>,
        val valueToString: (T) -> String,
    ) : FormContentViewModel() {
        constructor(
            valueProperty: ObservableValue<T>,
            optionList: List<T>,
            description: String = "",
            enabledProperty: ObservableValue<Boolean> = constObservable(true),
            valueToString: (T) -> String = { it.toString() }
        ) : this(
            valueProperty,
            optionList,
            description.observeConst(),
            enabledProperty,
            valueToString,
        )

        companion object {
            inline operator fun <reified T : Enum<T>> invoke(
                valueProperty: ObservableValue<T>,
                description: String = "",
                enabledProperty: ObservableValue<Boolean> = constObservable(true),
                noinline valueToString: (T) -> String = { it.toString() }
            ) = EnumInput(
                valueProperty,
                enumValues<T>().toList(),
                description.observeConst(),
                enabledProperty,
                valueToString,
            )
        }

        val stringOptionList = optionList.map {
            valueToString(it)
        }

        private val map = optionList.associateBy(valueToString)
        val stringValueProperty = property<String>(
            getter = {
                valueToString(valueProperty.value)
            },
            setter = { value ->
                val typedValue = map[value]
                if (typedValue != null && valueProperty is ObservableProperty<T> ) {
                    valueProperty.value = typedValue
                }
            },
            valueProperty
        )
    }

    class BooleanInput(
        val valueProperty: ObservableValue<Boolean>,
        val descriptionProperty: ObservableValue<String>,
        val enabledProperty: ObservableValue<Boolean>,
    ) : FormContentViewModel() {
        constructor(
            valueProperty: ObservableValue<Boolean>,
            description: String = "",
            enabledProperty: ObservableValue<Boolean> = constObservable(true),
        ) : this(
            valueProperty,
            description.observeConst(),
            enabledProperty,
        )
    }

    class Button(
        val contentProperty: ObservableValue<FormButtonContentViewModel>,
        val descriptionProperty: ObservableValue<String>,
        val enabledProperty: ObservableValue<Boolean>,
        val onClick: (event: ClickEvent) -> Unit,
    ) : FormContentViewModel() {
        constructor(
            icon: MaterialIcon,
            description: String = "",
            enabledProperty: ObservableValue<Boolean> = constObservable(true),
            onClick: (event: ClickEvent) -> Unit,
        ) : this(
            FormButtonContentViewModel(icon).observeConst(),
            description.observeConst(),
            enabledProperty,
            onClick
        )

        constructor(
            iconProperty: ObservableValue<MaterialIcon>,
            description: String = "",
            enabledProperty: ObservableValue<Boolean> = constObservable(true),
            onClick: (event: ClickEvent) -> Unit,
        ) : this(
            iconProperty.mapBinding { FormButtonContentViewModel(it) },
            description.observeConst(),
            enabledProperty,
            onClick
        )

        constructor(
            text: String,
            description: String = "",
            enabledProperty: ObservableValue<Boolean> = constObservable(true),
            onClick: (event: ClickEvent) -> Unit,
        ) : this(
            FormButtonContentViewModel(text).observeConst(),
            description.observeConst(),
            enabledProperty,
            onClick
        )

        constructor(
            textProperty: ObservableValue<String>,
            description: String = "",
            enabledProperty: ObservableValue<Boolean> = constObservable(true),
            onClick: (event: ClickEvent) -> Unit,
        ) : this(
            textProperty.mapBinding { FormButtonContentViewModel(it) },
            description.observeConst(),
            enabledProperty,
            onClick
        )
    }

    class ToggleButton(
        val valueProperty: ObservableValue<Boolean>,
        val contentProperty: ObservableValue<FormButtonContentViewModel>,
        val descriptionProperty: ObservableValue<String>,
        val enabledProperty: ObservableValue<Boolean>,
        val onClick: (event: ClickEvent) -> Unit,
    ) : FormContentViewModel() {
        constructor(
            valueProperty: ObservableValue<Boolean>,
            icon: MaterialIcon,
            description: String = "",
            enabledProperty: ObservableValue<Boolean> = constObservable(true),
            onClick: (event: ClickEvent) -> Unit,
        ) : this(
            valueProperty,
            FormButtonContentViewModel(icon).observeConst(),
            description.observeConst(),
            enabledProperty,
            onClick
        )

        constructor(
            valueProperty: ObservableValue<Boolean>,
            iconProperty: ObservableValue<MaterialIcon>,
            description: String = "",
            enabledProperty: ObservableValue<Boolean> = constObservable(true),
            onClick: (event: ClickEvent) -> Unit,
        ) : this(
            valueProperty,
            iconProperty.mapBinding { FormButtonContentViewModel(it) },
            description.observeConst(),
            enabledProperty,
            onClick
        )

        constructor(
            valueProperty: ObservableValue<Boolean>,
            text: String,
            description: String = "",
            enabledProperty: ObservableValue<Boolean> = constObservable(true),
            onClick: (event: ClickEvent) -> Unit,
        ) : this(
            valueProperty,
            FormButtonContentViewModel(text).observeConst(),
            description.observeConst(),
            enabledProperty,
            onClick
        )

        constructor(
            valueProperty: ObservableValue<Boolean>,
            textProperty: ObservableValue<String>,
            description: String = "",
            enabledProperty: ObservableValue<Boolean> = constObservable(true),
            onClick: (event: ClickEvent) -> Unit,
        ) : this(
            valueProperty,
            textProperty.mapBinding { FormButtonContentViewModel(it) },
            description.observeConst(),
            enabledProperty,
            onClick
        )

        constructor(
            valueProperty: ObservableProperty<Boolean>,
            icon: MaterialIcon,
            description: String = "",
            enabledProperty: ObservableValue<Boolean> = constObservable(true),
        ) : this(
            valueProperty.readOnly(),
            FormButtonContentViewModel(icon).observeConst(),
            description.observeConst(),
            enabledProperty,
            { valueProperty.toggle() },
        )

        constructor(
            valueProperty: ObservableProperty<Boolean>,
            iconProperty: ObservableValue<MaterialIcon>,
            description: String = "",
            enabledProperty: ObservableValue<Boolean> = constObservable(true),
        ) : this(
            valueProperty,
            iconProperty.mapBinding { FormButtonContentViewModel(it) },
            description.observeConst(),
            enabledProperty,
            { valueProperty.toggle() },
        )

        constructor(
            valueProperty: ObservableProperty<Boolean>,
            text: String,
            description: String = "",
            enabledProperty: ObservableValue<Boolean> = constObservable(true),
        ) : this(
            valueProperty,
            FormButtonContentViewModel(text).observeConst(),
            description.observeConst(),
            enabledProperty,
            { valueProperty.toggle() },
        )

        constructor(
            valueProperty: ObservableProperty<Boolean>,
            textProperty: ObservableValue<String>,
            description: String = "",
            enabledProperty: ObservableValue<Boolean> = constObservable(true),
        ) : this(
            valueProperty,
            textProperty.mapBinding { FormButtonContentViewModel(it) },
            description.observeConst(),
            enabledProperty,
            { valueProperty.toggle() },
        )
    }

    class Label(
        val contentProperty: ObservableValue<FormButtonContentViewModel>,
        val descriptionProperty: ObservableValue<String>,
    ) : FormContentViewModel() {
        constructor(
            icon: MaterialIcon,
            description: String = "",
        ) : this(
            FormButtonContentViewModel(icon).observeConst(),
            description.observeConst(),
        )

        constructor(
            iconProperty: ObservableValue<MaterialIcon>,
            description: String = "",
        ) : this(
            iconProperty.mapBinding { FormButtonContentViewModel(it) },
            description.observeConst(),
        )

        constructor(
            text: String,
            description: String = "",
        ) : this(
            FormButtonContentViewModel(text).observeConst(),
            description.observeConst(),
        )

        constructor(
            textProperty: ObservableValue<String>,
            description: String = "",
        ) : this(
            textProperty.mapBinding { FormButtonContentViewModel(it) },
            description.observeConst(),
        )
    }

    class Group(
        val contentProperty: ObservableList<FormContentViewModel>,
    ) : FormContentViewModel() {
        constructor(
            vararg content: FormContentViewModel,
        ) : this(
            observableListOf(*content),
        )
    }

    enum class InputTypeHint {
        TEXT, URL, PASSWORD, SEARCH
    }

    data class ClickEvent(
        val position: Vector,
        val screen: Dimension,
        val ctrlKey: Boolean = false,
        val altKey: Boolean = false,
        val shiftKey: Boolean = false,
    )
}
