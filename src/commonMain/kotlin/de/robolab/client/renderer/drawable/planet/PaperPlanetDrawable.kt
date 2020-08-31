package de.robolab.client.renderer.drawable.planet

import de.robolab.client.renderer.drawable.edit.PaperBackgroundDrawable
import de.robolab.client.renderer.utils.Transformation
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.property.property

class PaperPlanetDrawable(
    transformationStateProperty: ObservableProperty<Transformation.State> = property(Transformation.State.DEFAULT)
) : AbsPlanetDrawable(transformationStateProperty) {

    private val planetLayer = PlanetLayer("Planet layer")
    private val paperBackground = PaperBackgroundDrawable()

    fun importPlanet(planet: Planet) {
        planetLayer.importPlanet(planet)

        paperBackground.importPlanet(planet)
        importPlanets()
    }

    init {
        drawBackground = false

        setPlanetLayers(planetLayer)

        backgroundViews.add(paperBackground.backgroundView)
        underlayerViews.add(paperBackground.measuringView)
        overlayerViews.add(paperBackground.handlerView)
    }
}
