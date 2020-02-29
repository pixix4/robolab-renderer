package de.robolab.model

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
}
