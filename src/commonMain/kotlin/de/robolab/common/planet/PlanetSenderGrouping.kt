package de.robolab.common.planet

import de.robolab.common.planet.utils.IPlanetValue
import de.robolab.common.planet.utils.rotate
import de.robolab.common.planet.utils.translate
import kotlinx.serialization.Serializable

@Serializable
data class PlanetSenderGrouping(
    val name: String,
    val sender: Set<PlanetPoint>
) : IPlanetValue<PlanetSenderGrouping> {

    override fun translate(delta: PlanetPoint) = copy(
        sender = sender.translate(delta)
    )

    override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint) = copy(
        sender = sender.rotate(direction, origin)
    )
}