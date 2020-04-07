package de.robolab.renderer.drawable.planet

import de.robolab.planet.Planet
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.drawable.live.RobotDrawable

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
