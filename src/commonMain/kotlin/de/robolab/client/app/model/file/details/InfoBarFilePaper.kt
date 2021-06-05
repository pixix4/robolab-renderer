package de.robolab.client.app.model.file.details

import de.robolab.client.app.controller.ui.UiController
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.app.viewmodel.SideBarContentViewModel
import de.robolab.client.app.viewmodel.ViewModel
import de.robolab.client.app.viewmodel.buildForm
import de.robolab.client.app.viewmodel.buildFormContent
import de.robolab.client.renderer.drawable.general.PointAnimatableManager
import de.robolab.client.renderer.drawable.planet.PaperPlanetDrawable
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.planet.PlanetPath
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.mapBinding

class InfoBarFilePaper(
    private val planetEntry: FilePlanetDocument,
    val uiController: UiController,
) : FilePlanetDocument.FilePlanetSideBarTab<PaperPlanetDrawable>(
    "Paper",
    MaterialIcon.SQUARE_FOOT
), SideBarContentViewModel {

    override val drawable = PaperPlanetDrawable(planetEntry.transformationStateProperty)

    override fun importPlanet(planet: Planet) {
        drawable.importPlanet(planet)
    }

    override val parent: SideBarContentViewModel? = null
    override val contentProperty: ObservableValue<SideBarContentViewModel> = constObservable(this)

    override val topToolBar = buildFormContent { }
    override val bottomToolBar = buildFormContent { }

    val topContent = buildForm {
        labeledEntry("Orientation") {
            select(PreferenceStorage.paperOrientationProperty)
        }
        labeledEntry("Grid width") {
            input(PreferenceStorage.paperGridWidthProperty, 0.1..1000.0, 0.001)
        }
        labeledEntry("Paper strip width") {
            input(PreferenceStorage.paperStripWidthProperty, 0.1..1000.0, 0.001)
        }
        labeledEntry("Minimal padding") {
            input(PreferenceStorage.paperMinimalPaddingProperty, 0.1..1000.0, 0.001)
        }
        labeledEntry("Precision") {
            input(PreferenceStorage.paperPrecisionProperty, 0..10)
        }
    }

    private val statisticsDetailBox = PlanetStatisticsDetailBox(planetEntry.planetFile)
    val detailBoxProperty: ObservableValue<ViewModel> = drawable.focusedElementsProperty.mapBinding { list ->
        when (val first = list.firstOrNull()) {
            is PointAnimatableManager.AttributePoint -> PointDetailBox(first, planetEntry.planetFile)
            is PlanetPath -> PathDetailBox(first, planetEntry.planetFile)
            else -> statisticsDetailBox
        }
    }
}
