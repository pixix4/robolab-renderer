package de.robolab.client.app.model.base

import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.constObservable

class ToolBarEntry(
    val nameProperty: ObservableValue<String> = constObservable(""),
    val iconProperty: ObservableValue<MaterialIcon?> = constObservable(null),
    val toolTipProperty: ObservableValue<String> = constObservable(""),
    val selectedProperty: ObservableValue<Boolean> = constObservable(false),
    val enabledProperty: ObservableValue<Boolean> = constObservable(true),
    val onClick: () -> Unit = {}
)
