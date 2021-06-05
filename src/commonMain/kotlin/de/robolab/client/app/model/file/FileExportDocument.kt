package de.robolab.client.app.model.file

import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.app.viewmodel.FormContentViewModel
import de.robolab.client.app.viewmodel.SideBarTabViewModel
import de.robolab.client.renderer.drawable.planet.SimplePlanetDrawable
import de.robolab.common.planet.PlanetFile
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property

class FileExportDocument(planetFile: PlanetFile) : IPlanetDocument {
    override val nameProperty = constObservable("")
    override val toolBarLeft = constObservable<List<FormContentViewModel>>(emptyList())
    override val toolBarRight = constObservable<List<FormContentViewModel>>(emptyList())

    override val canUndoProperty = constObservable(false)
    override fun undo() {
    }

    override val canRedoProperty = constObservable(false)
    override fun redo() {
    }

    override val infoBarTabs: List<SideBarTabViewModel> = emptyList()
    override val activeTabProperty: ObservableProperty<SideBarTabViewModel?> = property()

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
