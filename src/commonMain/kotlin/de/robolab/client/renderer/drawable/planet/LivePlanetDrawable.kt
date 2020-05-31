package de.robolab.client.renderer.drawable.planet

import de.robolab.client.renderer.drawable.live.RobotDrawable
import de.robolab.common.planet.Planet

class LivePlanetDrawable : AbsPlanetDrawable() {

    private val backgroundLayer = PlanetLayer("Background layer") {
        it.withAlpha(0.2)
    }
    private val mqttLayer = PlanetLayer("Mqtt layer") {
        it.withAlpha(0.5)
    }
    private val serverLayer = PlanetLayer("Server layer")

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

        overlayerViews.add(robotDrawable.view)
    }
}
