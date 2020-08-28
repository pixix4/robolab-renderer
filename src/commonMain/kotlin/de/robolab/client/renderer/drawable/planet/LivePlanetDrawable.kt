package de.robolab.client.renderer.drawable.planet

import de.robolab.client.renderer.drawable.live.RobotDrawable
import de.robolab.client.renderer.utils.Transformation
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.property.property

class LivePlanetDrawable(
    transformationState: ObservableProperty<Transformation.State> = property(Transformation.State.DEFAULT)
) : AbsPlanetDrawable(transformationState) {

    private val backgroundLayer = PlanetLayer("Background layer") {
        it.withAlpha(0.2)
    }
    private val mqttLayer = PlanetLayer("Mqtt layer") {
        it.withAlpha(0.5)
    }
    private val serverLayer = PlanetLayer("Server layer")

    private val robotDrawable = RobotDrawable()

    fun importBackgroundPlanet(planet: Planet, isPartialUpdate: Boolean = false) {
        backgroundLayer.importPlanet(planet)
        if (!isPartialUpdate) {
            importPlanets()
        }
    }

    fun importMqttPlanet(planet: Planet, isPartialUpdate: Boolean = false) {
        mqttLayer.importPlanet(planet.importSplines(backgroundLayer.planet))
        if (!isPartialUpdate) {
            importPlanets()
        }
    }

    fun importServerPlanet(planet: Planet, isPartialUpdate: Boolean = false) {
        serverLayer.importPlanet(planet.importSplines(backgroundLayer.planet))
        if (!isPartialUpdate) {
            importPlanets()
        }
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
