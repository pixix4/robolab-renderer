package de.robolab.client.app.model.file

import de.robolab.client.app.model.base.IInfoBarContent
import de.robolab.client.renderer.drawable.general.PointAnimatableManager
import de.robolab.common.planet.Path
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.mapBinding

class InfoBarFilePaper(private val planetEntry: FileEntryPlanetDocument) :
    IInfoBarContent {

    private val statisticsDetailBox = PlanetStatisticsDetailBox(planetEntry.planetFile)
    val detailBoxProperty: ObservableValue<Any> = planetEntry.documentProperty.flatMapBinding {
        it.drawable.focusedElementsProperty
    }.mapBinding { list ->
        when (val first = list.firstOrNull()) {
            is PointAnimatableManager.AttributePoint -> PointDetailBox(first, planetEntry.planetFile)
            is Path -> PathDetailBox(first, planetEntry.planetFile)
            else -> statisticsDetailBox
        }
    }
}
