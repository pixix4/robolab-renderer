package de.robolab.common.planet.utils

import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetPoint

interface IPlanetValue<T: IPlanetValue<T>> {

    @Suppress("UNCHECKED_CAST")
    fun translate(delta: PlanetPoint): T = this as T

    @Suppress("UNCHECKED_CAST")
    fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint): T = this as T

    @Suppress("UNCHECKED_CAST")
    fun scaleWeights(factor: Double, offset: Long): T = this as T
}
