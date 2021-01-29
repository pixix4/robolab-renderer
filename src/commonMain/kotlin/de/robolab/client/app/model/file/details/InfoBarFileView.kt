package de.robolab.client.app.model.file.details

import de.robolab.client.app.model.base.IInfoBarContent
import de.robolab.client.app.model.file.FilePlanetDocument
import de.robolab.client.renderer.drawable.general.PointAnimatableManager
import de.robolab.client.renderer.drawable.planet.SimplePlanetDrawable
import de.robolab.common.planet.Path
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.mapBinding

class InfoBarFileView(private val planetEntry: FilePlanetDocument, viewDrawable: SimplePlanetDrawable) :
    IInfoBarContent {

    private val statisticsDetailBox = PlanetStatisticsDetailBox(planetEntry.planetFile)
    val detailBoxProperty: ObservableValue<Any> = viewDrawable.focusedElementsProperty.mapBinding { list ->
        when (val first = list.firstOrNull()) {
            is PointAnimatableManager.AttributePoint -> PointDetailBox(first, planetEntry.planetFile)
            is Path -> PathDetailBox(first, planetEntry.planetFile)
            else -> statisticsDetailBox
        }
    }
}
