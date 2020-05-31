package de.robolab.client.renderer.drawable.planet

import de.robolab.common.planet.Planet

class SimplePlanetDrawable : AbsPlanetDrawable() {

    private val planetLayer = PlanetLayer("Planet layer")

    fun importPlanet(planet: Planet) {
        planetLayer.importPlanet(planet)
        importPlanets()
    }

    init {
        setPlanetLayers(planetLayer)
    }
}
