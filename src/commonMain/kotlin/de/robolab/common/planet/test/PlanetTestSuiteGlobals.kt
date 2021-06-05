package de.robolab.common.planet.test

import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetPoint
import de.robolab.common.planet.utils.IPlanetValue
import de.robolab.common.planet.utils.rotate
import de.robolab.common.planet.utils.translate
import kotlinx.serialization.Serializable

@Serializable
data class PlanetTestSuiteGlobals(
    val flags: List<PlanetFlagSetter>,
    val tasks: List<PlanetSubscribableRef>,
) : IPlanetValue<PlanetTestSuiteGlobals> {

    override fun translate(delta: PlanetPoint) = copy(
        flags = flags.translate(delta),
        tasks = tasks.translate(delta),
    )

    override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint) = copy(
        flags = flags.rotate(direction, origin),
        tasks = tasks.rotate(direction, origin),
    )
}
