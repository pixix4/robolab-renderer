package de.robolab.client.app.model

import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.constObservable

class ToolBarEntry(
    val nameProperty: ObservableValue<String> = constObservable(""),
    val iconProperty: ObservableValue<Icon?> = constObservable(null),
    val toolTipProperty: ObservableValue<String> = constObservable(""),
    val selectedProperty: ObservableValue<Boolean> = constObservable(false),
    val enabledProperty: ObservableValue<Boolean> = constObservable(true),
    val onClick: () -> Unit = {}
) {

    enum class Icon {
        UNDO,
        REDO,
        PREFERENCES,
        FLIP
    }
}
