package de.robolab.common.planet.utils

import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetPoint


fun<T: IPlanetValue<T>> List<T>.translate(delta: PlanetPoint) = map { it.translate(delta) }
fun<T: IPlanetValue<T>> List<T>.rotate(direction: Planet.RotateDirection, origin: PlanetPoint) = map { it.rotate(direction, origin) }
fun<T: IPlanetValue<T>> List<T>.scaleWeights(factor: Double, offset: Long) = map { it.scaleWeights(factor, offset) }

fun<T: IPlanetValue<T>> Set<T>.translate(delta: PlanetPoint) = map { it.translate(delta) }.toSet()
fun<T: IPlanetValue<T>> Set<T>.rotate(direction: Planet.RotateDirection, origin: PlanetPoint) = map { it.rotate(direction, origin) }.toSet()
fun<T: IPlanetValue<T>> Set<T>.scaleWeights(factor: Double, offset: Long) = map { it.scaleWeights(factor, offset) }.toSet()

fun<T: IPlanetValue<T>> T?.translate(delta: PlanetPoint) = this?.translate(delta)
fun<T: IPlanetValue<T>> T?.rotate(direction: Planet.RotateDirection, origin: PlanetPoint) = this?.rotate(direction, origin)
fun<T: IPlanetValue<T>> T?.scaleWeights(factor: Double, offset: Long) = this?.scaleWeights(factor, offset)
