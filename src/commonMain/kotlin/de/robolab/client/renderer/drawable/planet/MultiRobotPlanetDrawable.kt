package de.robolab.client.renderer.drawable.planet

import de.robolab.client.renderer.drawable.live.RobotDrawable
import de.robolab.client.renderer.drawable.live.RobotDrawableManager
import de.robolab.common.planet.Planet

class MultiRobotPlanetDrawable : AbsPlanetDrawable() {

    private val planetLayer = PlanetLayer("Server layer")

    val robotDrawableManager = RobotDrawableManager()

    fun importPlanet(planet: Planet) {
        planetLayer.importPlanet(planet)
        importPlanets()
    }

    fun importRobots(robots: List<RobotDrawable.Robot>) {
        robotDrawableManager.robotList = robots
        robotDrawableManager.importPlanet(planetLayer.planet)
    }

    init {
        setPlanetLayers(planetLayer)

        overlayerViews.add(robotDrawableManager.view)
    }
}
