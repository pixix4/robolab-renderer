package de.robolab.client.app.model.base

import de.robolab.client.app.controller.InfoBarController
import de.robolab.client.renderer.view.base.Document
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue

interface IPlanetDocument {

    val nameProperty: ObservableValue<String>

    val toolBarLeft: ObservableValue<List<List<ToolBarEntry>>>
    val toolBarRight: ObservableValue<List<List<ToolBarEntry>>>

    val canUndoProperty: ObservableValue<Boolean>
    fun undo()
    val canRedoProperty: ObservableValue<Boolean>
    fun redo()

    val infoBarProperty: ObservableValue<IInfoBarContent?>
    val infoBarTabsProperty: ObservableValue<List<InfoBarController.Tab>?>
    val infoBarActiveTabProperty: ObservableValue<InfoBarController.Tab?>

    val documentProperty: ObservableValue<Document>

    fun onCreate()
    fun onAttach()
    fun onDetach()
    fun onDestroy()
}
