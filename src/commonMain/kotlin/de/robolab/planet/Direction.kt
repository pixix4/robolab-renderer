package de.robolab.model

import de.robolab.renderer.data.Point

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

    fun toVector() = when (this) {
        NORTH -> Point(0.0, 1.0)
        EAST -> Point(1.0, 0.0)
        SOUTH -> Point(0.0, -1.0)
        WEST -> Point(-1.0, 0.0)
    }
}
