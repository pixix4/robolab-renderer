package de.robolab.app.model

import de.robolab.renderer.drawable.base.IDrawable
import de.westermann.kobserve.ReadOnlyProperty
import de.westermann.kobserve.property.constProperty
import de.westermann.kobserve.property.property

interface ISideBarPlottable: ISideBarEntry {

    val toolBarLeft: List<List<ToolBarEntry>>
    val toolBarRight: List<List<ToolBarEntry>>

    val drawable: IDrawable

    val enabledProperty: ReadOnlyProperty<Boolean>
    fun onOpen() {}
}
