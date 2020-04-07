package de.robolab.renderer.drawable.planet

import de.robolab.planet.Planet
import de.robolab.renderer.drawable.live.RobotDrawable

class LivePlanetDrawable() : AbsPlanetDrawable() {

    private val backgroundLayer = PlanetLayer(this)
    private val mqttLayer = PlanetLayer(this)
    private val serverLayer = PlanetLayer(this)

    private val robotDrawable = RobotDrawable(this)

    fun importBackgroundPlanet(planet: Planet) {
        backgroundLayer.importPlanet(planet)
        importPlanets()
    }

    fun importMqttPlanet(planet: Planet) {
        mqttLayer.importPlanet(planet.importSplines(backgroundLayer.planet))
        importPlanets()
    }


    fun importServerPlanet(planet: Planet) {
        serverLayer.importPlanet(planet.importSplines(backgroundLayer.planet))
        importPlanets()
    }

    fun importRobot(robot: RobotDrawable.Robot?) {
        robotDrawable.importRobot(backgroundLayer.planet, robot)
    }

    init {
        buildDrawableList(
                planetLayers = listOf(
                        backgroundLayer,
                        mqttLayer,
                        serverLayer
                ),
                overlays = listOf(
                        robotDrawable
                )
        )
    }
}
