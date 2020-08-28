package de.robolab.client.renderer.drawable.planet

import de.robolab.client.renderer.utils.Transformation
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.property.property

class SimplePlanetDrawable(
    transformationStateProperty: ObservableProperty<Transformation.State> = property(Transformation.State.DEFAULT)
) : AbsPlanetDrawable(transformationStateProperty) {

    private val planetLayer = PlanetLayer("Planet layer")

    fun importPlanet(planet: Planet) {
        planetLayer.importPlanet(planet)
        importPlanets()
    }

    init {
        setPlanetLayers(planetLayer)
    }
}
