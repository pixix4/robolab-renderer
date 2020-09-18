package de.robolab.common.planet

import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class Coordinate(val x: Int, val y: Int): IPlanetValue {

    fun getColor(bluePoint: Coordinate?): Color {
        if (bluePoint == null) {
            return Color.UNKNOWN
        }

        if (((this.x + this.y) % 2 == 0) == ((bluePoint.x + bluePoint.y) % 2 == 0)) {
            return Color.BLUE
        }
        return Color.RED
    }

    fun translate(delta: Coordinate) = Coordinate(x + delta.x, y + delta.y)

    fun rotate(direction: Planet.RotateDirection, origin: Coordinate): Coordinate {
        return Coordinate(
            ((x - origin.x) * cos(direction.angle) - (y - origin.y) * sin(direction.angle)).roundToInt() + origin.x,
            ((x - origin.x) * sin(direction.angle) + (y - origin.y) * cos(direction.angle)).roundToInt() + origin.y
        )
    }

    fun toSimpleString() = "$x, $y"

    enum class Color {
        RED, BLUE, UNKNOWN
    }
}