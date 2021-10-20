package de.robolab.client.utils

import de.robolab.client.app.controller.ui.InfoBarController
import de.robolab.client.app.model.base.IInfoBarContent
import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.app.model.base.ToolBarEntry
import de.robolab.client.app.viewmodel.FormContentViewModel
import de.robolab.client.app.viewmodel.SideBarTabViewModel
import de.robolab.client.renderer.view.base.Document
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property

class HeadlessPlanetDocument(
    document: Document
): IPlanetDocument {

    override val nameProperty = constObservable("")
    override val toolBarLeft: ObservableValue<List<FormContentViewModel>> = constObservable(emptyList())
    override val toolBarRight: ObservableValue<List<FormContentViewModel>> = constObservable(emptyList())

    override val infoBarTabs: List<SideBarTabViewModel> = listOf()
    override val activeTabProperty: ObservableProperty<SideBarTabViewModel?> = property()

    override val canUndoProperty = constObservable(false)

    override fun undo() {
    }

    override val canRedoProperty = constObservable(false)

    override fun redo() {
    }

    override val documentProperty: ObservableValue<Document> = constObservable(document)

    override fun onCreate() {
    }

    override fun onAttach() {
    }

    override fun onDetach() {
    }

    override fun onDestroy() {
    }

    override fun centerPlanet() {
    }
}
