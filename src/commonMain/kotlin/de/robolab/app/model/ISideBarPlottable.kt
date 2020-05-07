package de.robolab.app.model

import de.robolab.renderer.drawable.base.IDrawable
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue

interface ISideBarPlottable: ISideBarEntry {

    val toolBarLeft: List<List<ToolBarEntry>>
    val toolBarRight: List<List<ToolBarEntry>>

    val infoBarList: List<IInfoBarContent>
    
    val selectedInfoBarIndexProperty: ObservableProperty<Int?>

    val drawable: IDrawable

    val enabledProperty: ObservableValue<Boolean>
    fun onOpen() {}
}
