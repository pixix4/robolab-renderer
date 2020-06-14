package de.robolab.common.planet

import de.robolab.common.utils.Point
import de.robolab.common.utils.Vector
import kotlin.math.absoluteValue

enum class Direction: IPlanetValue {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    fun opposite() = when (this) {
        NORTH -> SOUTH
        EAST -> WEST
        SOUTH -> NORTH
        WEST -> EAST
    }

    fun turnClockwise() = when (this) {
        NORTH -> EAST
        EAST -> SOUTH
        SOUTH -> WEST
        WEST -> NORTH
    }

    fun turnCounterClockwise() = when (this) {
        NORTH -> WEST
        EAST -> NORTH
        SOUTH -> EAST
        WEST -> SOUTH
    }

    fun toVector(size: Double = 1.0) = when (this) {
        NORTH -> Point(0.0, size)
        EAST -> Point(size, 0.0)
        SOUTH -> Point(0.0, -size)
        WEST -> Point(-size, 0.0)
    }

    fun rotate(direction: Planet.RotateDirection) = when(direction) {
        Planet.RotateDirection.CLOCKWISE -> turnClockwise()
        Planet.RotateDirection.COUNTER_CLOCKWISE -> turnCounterClockwise()
    }

    companion object {
        fun fromVector(vector: Vector): Direction {
            return if (vector.x.absoluteValue < vector.y.absoluteValue) {
                if (vector.y >= 0) NORTH else SOUTH
            } else {
                if (vector.x >= 0) EAST else WEST
            }
        }
    }
}

fun Direction?.letter(): Char = this?.name?.first() ?: '#'
