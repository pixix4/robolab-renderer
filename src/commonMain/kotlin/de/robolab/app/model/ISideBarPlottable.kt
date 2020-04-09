package de.robolab.app.model

import de.robolab.renderer.drawable.base.IDrawable
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.property.constProperty
import de.westermann.kobserve.property.property

interface ISideBarPlottable: ISideBarEntry {

    val actionList: List<List<PlottableAction>>

    val drawable: IDrawable

    val enabledProperty: ReadOnlyProperty<Boolean>
    fun onOpen() {}

    val canUndoProperty: ReadOnlyProperty<Boolean>
    fun undo()

    val canRedoProperty: ReadOnlyProperty<Boolean>
    fun redo()

    class PlottableAction(
            val nameProperty: ReadOnlyProperty<String>,
            val activeProperty: ReadOnlyProperty<Boolean>,
            val onClick: () -> Unit
    ) {
        constructor(name: String, activeProperty: ReadOnlyProperty<Boolean>, onClick: () -> Unit): this(constProperty(name), activeProperty, onClick)
        constructor(name: String, onClick: () -> Unit): this(constProperty(name), constProperty(false), onClick)
    }
}
