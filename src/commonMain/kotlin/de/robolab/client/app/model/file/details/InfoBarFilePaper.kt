package de.robolab.client.app.model.file.details

import de.robolab.client.app.model.base.IInfoBarContent
import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.renderer.drawable.general.PointAnimatableManager
import de.robolab.client.renderer.drawable.planet.PaperPlanetDrawable
import de.robolab.common.planet.Path
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.mapBinding

class InfoBarFilePaper(private val planetEntry: FilePlanetDocument, paperDrawable: PaperPlanetDrawable) :
    IInfoBarContent {

    private val statisticsDetailBox = PlanetStatisticsDetailBox(planetEntry.planetFile)
    val detailBoxProperty: ObservableValue<Any> = paperDrawable.focusedElementsProperty.mapBinding { list ->
        when (val first = list.firstOrNull()) {
            is PointAnimatableManager.AttributePoint -> PointDetailBox(first, planetEntry.planetFile)
            is Path -> PathDetailBox(first, planetEntry.planetFile)
            else -> statisticsDetailBox
        }
    }
}
