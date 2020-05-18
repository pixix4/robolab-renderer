package de.robolab.renderer.drawable.planet

import de.robolab.planet.Planet
import de.robolab.renderer.drawable.live.RobotDrawable

class LivePlanetDrawable() : AbsPlanetDrawable() {

    private val backgroundLayer = PlanetLayer {
        it.withAlpha(0.2)
    }
    private val mqttLayer = PlanetLayer()
    private val serverLayer = PlanetLayer()

    private val robotDrawable = RobotDrawable()

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
        setPlanetLayers(
                backgroundLayer,
                mqttLayer,
                serverLayer
        )

        //overlayerViews.addAll(robotDrawable)
    }
}
