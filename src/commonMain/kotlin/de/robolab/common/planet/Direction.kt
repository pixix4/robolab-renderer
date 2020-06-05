package de.robolab.common.planet

import de.robolab.common.utils.Point

enum class Direction {
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

    fun turnHigh() = when (this) {
        NORTH -> EAST
        EAST -> NORTH
        SOUTH -> WEST
        WEST -> SOUTH
    }

    fun turnLow() = when (this) {
        NORTH -> WEST
        EAST -> SOUTH
        SOUTH -> EAST
        WEST -> NORTH
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

    fun toVector() = when (this) {
        NORTH -> Point(0.0, 1.0)
        EAST -> Point(1.0, 0.0)
        SOUTH -> Point(0.0, -1.0)
        WEST -> Point(-1.0, 0.0)
    }

    fun rotate(direction: Planet.RotateDirection) = when(direction) {
        Planet.RotateDirection.CLOCKWISE -> turnClockwise()
        Planet.RotateDirection.COUNTER_CLOCKWISE -> turnCounterClockwise()
    }
}

fun Direction?.letter(): Char = this?.name?.first() ?: '#'
