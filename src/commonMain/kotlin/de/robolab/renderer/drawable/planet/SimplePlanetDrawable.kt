package de.robolab.renderer.drawable.planet

import de.robolab.planet.Planet

class SimplePlanetDrawable() : AbsPlanetDrawable() {

    private val planetLayer = PlanetLayer(this)

    fun importPlanet(planet: Planet) {
        planetLayer.importPlanet(planet)
        importPlanets()
    }

    init {
        buildDrawableList(
                planetLayers = listOf(
                        planetLayer
                )
        )
    }
}
