package de.robolab.client.app.model

import de.robolab.client.renderer.view.base.Document
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue

interface ISideBarPlottable : ISideBarEntry {

    val toolBarLeft: List<List<ToolBarEntry>>
    val toolBarRight: List<List<ToolBarEntry>>

    val infoBarList: List<IInfoBarContent>

    val selectedInfoBarIndexProperty: ObservableProperty<Int?>

    val document: Document

    val enabledProperty: ObservableValue<Boolean>
}
