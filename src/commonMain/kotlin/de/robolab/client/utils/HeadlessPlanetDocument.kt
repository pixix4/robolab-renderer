package de.robolab.client.utils

import de.robolab.client.app.controller.InfoBarController
import de.robolab.client.app.model.base.IInfoBarContent
import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.app.model.base.ToolBarEntry
import de.robolab.client.renderer.view.base.Document
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.constObservable

class HeadlessPlanetDocument(
    document: Document
): IPlanetDocument {

    override val nameProperty = constObservable("")
    override val toolBarLeft: ObservableValue<List<List<ToolBarEntry>>> = constObservable(emptyList())
    override val toolBarRight: ObservableValue<List<List<ToolBarEntry>>> = constObservable(emptyList())

    override val infoBarProperty = constObservable<IInfoBarContent>()
    override val infoBarTabsProperty = constObservable<List<InfoBarController.Tab>>()
    override val infoBarActiveTabProperty =  constObservable<InfoBarController.Tab>()

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
}
