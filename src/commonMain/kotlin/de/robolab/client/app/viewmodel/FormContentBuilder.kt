package de.robolab.client.app.viewmodel

import de.robolab.client.app.model.base.MaterialIcon
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.list.asObservable
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.observeConst

@FormBuilderAnnotation
class FormContentBuilder {

    val content = mutableListOf<FormContentViewModel>()

    fun input(
        valueProperty: ObservableValue<String>,
        description: String = "",
        typeHint: FormContentViewModel.InputTypeHint = FormContentViewModel.InputTypeHint.TEXT,
        enabledProperty: ObservableValue<Boolean> = constObservable(true),
        initBlock: FormContentViewModel.StringInput.() -> Unit = {}
    ): FormContentViewModel.StringInput {
        val result = FormContentViewModel.StringInput(valueProperty, description, typeHint, enabledProperty)
        initBlock(result)
        content += result
        return result
    }

    fun input(
        value: String,
        description: String = "",
        typeHint: FormContentViewModel.InputTypeHint = FormContentViewModel.InputTypeHint.TEXT,
        enabledProperty: ObservableValue<Boolean> = constObservable(true),
        initBlock: FormContentViewModel.StringInput.() -> Unit = {}
    ): FormContentViewModel.StringInput {
        val result = FormContentViewModel.StringInput(value.observeConst(), description, typeHint, enabledProperty)
        initBlock(result)
        content += result
        return result
    }

    fun input(
        valueProperty: ObservableValue<Int>,
        range: IntRange,
        description: String = "",
        enabledProperty: ObservableValue<Boolean> = constObservable(true),
    ): FormContentViewModel.IntInput {
        val result = FormContentViewModel.IntInput(valueProperty, range, description, enabledProperty)
        content += result
        return result
    }

    fun input(
        valueProperty: ObservableValue<Long>,
        range: LongRange,
        description: String = "",
        enabledProperty: ObservableValue<Boolean> = constObservable(true),
    ): FormContentViewModel.LongInput {
        val result = FormContentViewModel.LongInput(valueProperty, range, description, enabledProperty)
        content += result
        return result
    }

    fun input(
        valueProperty: ObservableValue<Double>,
        range: ClosedFloatingPointRange<Double>,
        step: Double,
        description: String = "",
        enabledProperty: ObservableValue<Boolean> = constObservable(true),
    ): FormContentViewModel.DoubleInput {
        val result = FormContentViewModel.DoubleInput(valueProperty, range, step, description, enabledProperty)
        content += result
        return result
    }

    fun input(
        valueProperty: ObservableValue<Boolean>,
        description: String = "",
        enabledProperty: ObservableValue<Boolean> = constObservable(true),
    ): FormContentViewModel.BooleanInput {
        val result = FormContentViewModel.BooleanInput(valueProperty, description, enabledProperty)
        content += result
        return result
    }

    fun select(
        valueProperty: ObservableValue<String>,
        optionList: List<String>,
        description: String = "",
        enabledProperty: ObservableValue<Boolean> = constObservable(true),
    ): FormContentViewModel.SelectInput {
        val result = FormContentViewModel.SelectInput(valueProperty, optionList, description, enabledProperty)
        content += result
        return result
    }

    inline fun <reified T : Enum<T>> select(
        valueProperty: ObservableValue<T>,
        description: String = "",
        enabledProperty: ObservableValue<Boolean> = constObservable(true),
        noinline valueToString: (T) -> String = { it.toString() },
    ): FormContentViewModel.EnumInput<T> {
        val result = FormContentViewModel.EnumInput(valueProperty, description, enabledProperty, valueToString)
        content += result
        return result
    }

    fun button(
        icon: MaterialIcon,
        description: String = "",
        enabledProperty: ObservableValue<Boolean> = constObservable(true),
        onClick: (event: FormContentViewModel.ClickEvent) -> Unit,
    ): FormContentViewModel.Button {
        val result = FormContentViewModel.Button(icon, description, enabledProperty, onClick)
        content += result
        return result
    }

    fun button(
        iconProperty: ObservableValue<MaterialIcon>,
        description: String = "",
        enabledProperty: ObservableValue<Boolean> = constObservable(true),
        onClick: (event: FormContentViewModel.ClickEvent) -> Unit,
    ): FormContentViewModel.Button {
        val result = FormContentViewModel.Button(iconProperty, description, enabledProperty, onClick)
        content += result
        return result
    }

    fun button(
        text: String,
        description: String = "",
        enabledProperty: ObservableValue<Boolean> = constObservable(true),
        onClick: (event: FormContentViewModel.ClickEvent) -> Unit,
    ): FormContentViewModel.Button {
        val result = FormContentViewModel.Button(text, description, enabledProperty, onClick)
        content += result
        return result
    }

    fun button(
        textProperty: ObservableValue<String>,
        description: String = "",
        enabledProperty: ObservableValue<Boolean> = constObservable(true),
        onClick: (event: FormContentViewModel.ClickEvent) -> Unit,
    ): FormContentViewModel.Button {
        val result = FormContentViewModel.Button(textProperty, description, enabledProperty, onClick)
        content += result
        return result
    }

    fun toggleButton(
        valueProperty: ObservableValue<Boolean>,
        icon: MaterialIcon,
        description: String = "",
        enabledProperty: ObservableValue<Boolean> = constObservable(true),
        onClick: (event: FormContentViewModel.ClickEvent) -> Unit,
    ): FormContentViewModel.ToggleButton {
        val result = FormContentViewModel.ToggleButton(valueProperty, icon, description, enabledProperty, onClick)
        content += result
        return result
    }

    fun toggleButton(
        valueProperty: ObservableValue<Boolean>,
        iconProperty: ObservableValue<MaterialIcon>,
        description: String = "",
        enabledProperty: ObservableValue<Boolean> = constObservable(true),
        onClick: (event: FormContentViewModel.ClickEvent) -> Unit,
    ): FormContentViewModel.ToggleButton {
        val result =
            FormContentViewModel.ToggleButton(valueProperty, iconProperty, description, enabledProperty, onClick)
        content += result
        return result
    }

    fun toggleButton(
        valueProperty: ObservableValue<Boolean>,
        text: String,
        description: String = "",
        enabledProperty: ObservableValue<Boolean> = constObservable(true),
        onClick: (event: FormContentViewModel.ClickEvent) -> Unit,
    ): FormContentViewModel.ToggleButton {
        val result = FormContentViewModel.ToggleButton(valueProperty, text, description, enabledProperty, onClick)
        content += result
        return result
    }

    fun toggleButton(
        valueProperty: ObservableValue<Boolean>,
        textProperty: ObservableValue<String>,
        description: String = "",
        enabledProperty: ObservableValue<Boolean> = constObservable(true),
        onClick: (event: FormContentViewModel.ClickEvent) -> Unit,
    ): FormContentViewModel.ToggleButton {
        val result =
            FormContentViewModel.ToggleButton(valueProperty, textProperty, description, enabledProperty, onClick)
        content += result
        return result
    }

    fun label(
        icon: MaterialIcon,
        description: String = "",
    ): FormContentViewModel.Label {
        val result = FormContentViewModel.Label(icon, description)
        content += result
        return result
    }

    fun label(
        iconProperty: ObservableValue<MaterialIcon>,
        description: String = "",
    ): FormContentViewModel.Label {
        val result = FormContentViewModel.Label(iconProperty, description)
        content += result
        return result
    }

    fun label(
        text: String,
        description: String = "",
    ): FormContentViewModel.Label {
        val result = FormContentViewModel.Label(text, description)
        content += result
        return result
    }

    fun label(
        textProperty: ObservableValue<String>,
        description: String = "",
    ): FormContentViewModel.Label {
        val result = FormContentViewModel.Label(textProperty, description)
        content += result
        return result
    }

    fun group(builder: FormContentBuilder.() -> Unit): FormContentViewModel.Group {
        val b = FormContentBuilder()
        builder(b)
        val result = b.buildGroup()
        content += result
        return result
    }

    fun buildGroup(): FormContentViewModel.Group {
        return FormContentViewModel.Group(content.asObservable())
    }

    fun build(): FormContentViewModel {
        return if (content.size == 1) {
            content[0]
        } else buildGroup()
    }
}

fun buildFormContent(builder: FormContentBuilder.() -> Unit): FormContentViewModel.Group {
    val b = FormContentBuilder()
    builder(b)
    return b.buildGroup()
}
