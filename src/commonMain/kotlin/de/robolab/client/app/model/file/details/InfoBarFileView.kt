package de.robolab.client.app.model.file.details

import de.robolab.client.app.controller.ui.UiController
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.app.model.file.boxes.PathDetailBox
import de.robolab.client.app.model.file.boxes.PlanetStatisticsDetailBox
import de.robolab.client.app.model.file.boxes.PointDetailBox
import de.robolab.client.app.viewmodel.SideBarContentViewModel
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.app.viewmodel.buildFormContent
import de.robolab.client.renderer.drawable.general.PointAnimatableManager
import de.robolab.client.renderer.drawable.planet.SimplePlanetDrawable
import de.robolab.common.planet.PlanetPath
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.mapBinding

class InfoBarFileView(
    private val planetEntry: FilePlanetDocument,
    val uiController: UiController,
) : FilePlanetDocument.FilePlanetSideBarTab<SimplePlanetDrawable>(
    "View",
    MaterialIcon.INFO_OUTLINE
), SideBarContentViewModel {

    override val drawable = SimplePlanetDrawable(planetEntry.transformationStateProperty)

    override fun importPlanet(planet: Planet) {
        drawable.importPlanet(planet)
    }

    override val parent: SideBarContentViewModel? = null
    override val contentProperty: ObservableValue<SideBarContentViewModel> = constObservable(this)

    override val topToolBar = buildFormContent { }
    override val bottomToolBar = buildFormContent { }

    private var pointDetailBox: PointDetailBox? = null
    private var pathDetailBox: PathDetailBox? = null
    private val statisticsDetailBox = PlanetStatisticsDetailBox(planetEntry.planetFile)

    val detailBoxProperty: ObservableValue<ViewModel> = drawable.focusedElementsProperty.mapBinding { list ->
        when (val first = list.firstOrNull()) {
            is PointAnimatableManager.AttributePoint -> {
                val box = pointDetailBox

                if (box == null) {
                    val b = PointDetailBox(first, planetEntry.planetFile)
                    pointDetailBox = b
                    b
                } else {
                    box.point = first
                    box
                }
            }
            is PlanetPath -> {
                val box = pathDetailBox

                if (box == null) {
                    val b = PathDetailBox(first, planetEntry.planetFile)
                    pathDetailBox = b
                    b
                } else {
                    box.path = first
                    box
                }
            }
            else -> {
                statisticsDetailBox
            }
        }
    }
}
