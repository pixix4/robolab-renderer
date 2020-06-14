package de.robolab.common.planet

import de.robolab.client.traverser.nextHexString
import kotlin.random.Random

inline class ID(val id: String): IPlanetValue

fun randomName(): String = "Planet-${Random.nextHexString(3)}-${Random.nextHexString(5)}"