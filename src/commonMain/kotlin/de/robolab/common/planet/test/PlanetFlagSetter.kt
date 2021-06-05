package de.robolab.common.planet.test

import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetPoint
import de.robolab.common.planet.utils.IPlanetValue
import de.robolab.common.planet.utils.rotate
import de.robolab.common.planet.utils.translate
import kotlinx.serialization.Serializable

@Serializable
data class PlanetFlagSetter(
    val ref: PlanetSubscribableRef? = null,
    val type: PlanetFlagType,
    val value: Boolean
) : IPlanetValue<PlanetFlagSetter> {

    override fun translate(delta: PlanetPoint) = copy(
        ref = ref.translate(delta)
    )

    override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint) = copy(
        ref = ref.rotate(direction, origin)
    )
}
