package de.robolab.client.app.model.base

import de.robolab.client.renderer.view.base.Document
import de.westermann.kobserve.base.ObservableValue

interface INavigationBarPlottable : INavigationBarEntry {

    val toolBarLeft: List<List<ToolBarEntry>>
    val toolBarRight: List<List<ToolBarEntry>>

    val infoBarProperty: ObservableValue<IInfoBarContent>
    val detailBoxProperty: ObservableValue<IDetailBox>

    val documentProperty: ObservableValue<Document>

    val enabledProperty: ObservableValue<Boolean>
}
