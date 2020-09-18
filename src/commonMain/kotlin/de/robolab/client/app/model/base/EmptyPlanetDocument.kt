package de.robolab.client.app.model.base

import de.robolab.client.app.controller.InfoBarController
import de.robolab.client.app.model.base.IInfoBarContent
import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.app.model.base.ToolBarEntry
import de.robolab.client.renderer.drawable.planet.SimplePlanetDrawable
import de.robolab.common.parser.PlanetFile
import de.westermann.kobserve.property.constObservable

class EmptyPlanetDocument() : IPlanetDocument {
    override val nameProperty = constObservable("")
    override val toolBarLeft = constObservable<List<List<ToolBarEntry>>>(emptyList())
    override val toolBarRight = constObservable<List<List<ToolBarEntry>>>(emptyList())

    override val canUndoProperty = constObservable(false)
    override fun undo() {
    }

    override val canRedoProperty = constObservable(false)
    override fun redo() {
    }

    override val infoBarProperty = constObservable<IInfoBarContent>()
    override val infoBarTabsProperty = constObservable<List<InfoBarController.Tab>>()
    override val infoBarActiveTabProperty = constObservable<InfoBarController.Tab>()

    override val documentProperty = constObservable(SimplePlanetDrawable().view)

    override fun onCreate() {
    }

    override fun onAttach() {
    }

    override fun onDetach() {
    }

    override fun onDestroy() {
    }
}
