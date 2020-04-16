package de.robolab.app.model

import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.property.constProperty

class ToolBarEntry(
        val nameProperty: ReadOnlyProperty<String> = constProperty(""),
        val iconProperty: ReadOnlyProperty<Icon?> = constProperty(null),
        val toolTipProperty: ReadOnlyProperty<String> = constProperty(""),
        val selectedProperty: ReadOnlyProperty<Boolean> = constProperty(false),
        val enabledProperty: ReadOnlyProperty<Boolean> = constProperty(true),
        val onClick: () -> Unit = {}
) {

    enum class Icon {
        UNDO,
        REDO,
        PREFERENCES
    }
}
