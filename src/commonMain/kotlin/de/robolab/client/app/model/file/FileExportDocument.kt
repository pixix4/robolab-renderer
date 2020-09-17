package de.robolab.client.app.model.file

import de.robolab.client.app.controller.InfoBarController
import de.robolab.client.app.model.base.IInfoBarContent
import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.app.model.base.ToolBarEntry
import de.robolab.client.renderer.drawable.planet.SimplePlanetDrawable
import de.robolab.common.parser.PlanetFile
import de.westermann.kobserve.property.constObservable

class FileExportDocument(planetFile: PlanetFile) : IPlanetDocument {
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

    val drawable = SimplePlanetDrawable()
    override val documentProperty = constObservable(drawable.view)

    override fun onCreate() {
    }

    override fun onAttach() {
    }

    override fun onDetach() {
    }

    override fun onDestroy() {
    }

    init {
        drawable.drawCompass = false
        drawable.drawName = true
        drawable.importPlanet(planetFile.planet)
    }

    fun center() {
        drawable.centerPlanet()
    }
}