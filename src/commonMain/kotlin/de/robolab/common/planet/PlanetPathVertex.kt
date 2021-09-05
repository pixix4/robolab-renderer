package de.robolab.common.planet

data class PlanetPathVertex(val point: PlanetPoint, val direction: PlanetDirection) {
    fun toShortString(): String = "${point.x},${point.y},${direction.letter}"
}
