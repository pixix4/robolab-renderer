package de.robolab.traverser

import de.robolab.planet.Coordinate
import de.robolab.planet.Direction

interface ITraverserTrail {
    val summary: String
        get() = path.map(Pair<Coordinate, Direction?>::second).joinToString {
            when (it) {
                Direction.EAST -> "E"
                Direction.NORTH -> "N"
                Direction.WEST -> "W"
                Direction.SOUTH -> "S"
                null -> "#"
            }
        }
    val locations: List<Coordinate>
        get() = path.map(Pair<Coordinate, Direction?>::first)
    val directions: List<Direction>
        get() = path.mapNotNull(Pair<Coordinate, Direction?>::second)
    val start: Coordinate
        get() = path.first().first
    val end: Coordinate
        get() = path.last().first
    val path: List<Pair<Coordinate, Direction?>>
    val result: TraverserState.Status
    val resultCause: Any?
}

data class TraverserTrail(override val path: List<Pair<Coordinate, Direction?>>,
                          override val result: TraverserState.Status,
                          override val resultCause: Any? = null) : ITraverserTrail {
    override fun toString(): String =
            "(${start.x}, ${start.y}) -> [$summary] -> (${end.x}, ${end.y}): $result" +
                    if (resultCause != null) " ($resultCause)"
                    else ""
}