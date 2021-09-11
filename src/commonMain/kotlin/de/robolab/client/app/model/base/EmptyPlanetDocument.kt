package de.robolab.client.app.model.base

import de.robolab.client.app.viewmodel.FormContentViewModel
import de.robolab.client.app.viewmodel.SideBarTabViewModel
import de.robolab.client.renderer.drawable.planet.SimplePlanetDrawable
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property

class EmptyPlanetDocument() : IPlanetDocument {
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

    override val documentProperty = constObservable(SimplePlanetDrawable().view)

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

    init {
        documentProperty.value.drawPlaceholder = true
    }
}
