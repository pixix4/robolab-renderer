package de.robolab.client.app.model.base

import de.robolab.client.app.viewmodel.FormContentViewModel
import de.robolab.client.app.viewmodel.SideBarTabViewModel
import de.robolab.client.renderer.view.base.Document
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue

interface IPlanetDocument {

    val nameProperty: ObservableValue<String>

    val toolBarLeft: ObservableValue<List<FormContentViewModel>>
    val toolBarRight: ObservableValue<List<FormContentViewModel>>

    val canUndoProperty: ObservableValue<Boolean>
    fun undo()
    val canRedoProperty: ObservableValue<Boolean>
    fun redo()

    val infoBarTabs: List<SideBarTabViewModel>
    val activeTabProperty: ObservableProperty<SideBarTabViewModel?>

    fun openTab(tab: SideBarTabViewModel) {
        if (tab in infoBarTabs) {
            activeTabProperty.value = tab
        }
    }

    fun centerPlanet()

    val documentProperty: ObservableValue<Document>

    fun onCreate()
    fun onAttach()
    fun onDetach()
    fun onDestroy()
}
