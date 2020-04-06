package de.robolab.app.model

import de.robolab.renderer.drawable.base.IDrawable
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.property.constProperty

interface IPlottable {

    val actionList: List<List<PlottableAction>>

    val nameProperty: ReadOnlyProperty<String>
    val statusProperty: ReadOnlyProperty<String>

    val drawable: IDrawable

    class PlottableAction(
            val nameProperty: ReadOnlyProperty<String>,
            val activeProperty: ReadOnlyProperty<Boolean>,
            val onClick: () -> Unit
    ) {
        constructor(name: String, activeProperty: ReadOnlyProperty<Boolean>, onClick: () -> Unit): this(constProperty(name), activeProperty, onClick)
        constructor(name: String, onClick: () -> Unit): this(constProperty(name), constProperty(false), onClick)
    }
}

