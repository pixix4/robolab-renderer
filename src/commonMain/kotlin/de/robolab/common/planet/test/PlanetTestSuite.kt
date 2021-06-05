package de.robolab.common.planet.test

import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetPoint
import de.robolab.common.planet.utils.IPlanetValue
import de.robolab.common.planet.utils.rotate
import de.robolab.common.planet.utils.translate
import kotlinx.serialization.Serializable

@Serializable
data class PlanetTestSuite(
    val goals: List<PlanetTestGoal>,
    val signalGroups: List<PlanetSignalGroup>,
    val globals: PlanetTestSuiteGlobals,
) : IPlanetValue<PlanetTestSuite> {

    override fun translate(delta: PlanetPoint) = copy(
        goals = goals.translate(delta),
        signalGroups = signalGroups.translate(delta),
        globals = globals.translate(delta)
    )

    override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint) = copy(
        goals = goals.rotate(direction, origin),
        signalGroups = signalGroups.rotate(direction, origin),
        globals = globals.rotate(direction, origin)
    )
}
