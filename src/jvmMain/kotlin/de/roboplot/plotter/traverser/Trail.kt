package de.roboplot.plotter.traverser

import de.roboplot.plotter.model.Direction
import de.roboplot.plotter.model.Point

interface ITraverserTrail {
    val summary: String
        get() = path.map(Pair<Point, Direction?>::second).joinToString {
            when (it) {
                Direction.EAST -> "E"
                Direction.NORTH -> "N"
                Direction.WEST -> "W"
                Direction.SOUTH -> "S"
                null -> "#"
                else -> "~"
            }
        }
    val locations: List<Point>
        get() = path.map(Pair<Point, Direction?>::first)
    val directions: List<Direction>
        get() = path.map(Pair<Point, Direction?>::second).filterNotNull()
    val start: Point
        get() = path.first().first
    val end: Point
        get() = path.last().first
    val path: List<Pair<Point, Direction?>>
    val result: TraverserState.Status
    val resultCause: Any?
}

data class TraverserTrail(override val path: List<Pair<Point, Direction?>>,
                          override val result: TraverserState.Status,
                          override val resultCause: Any? = null) : ITraverserTrail {
    override fun toString(): String =
            "(${start.x}, ${start.y}) -> [$summary] -> (${end.x}, ${end.y}): $result" +
                    if (resultCause != null) " ($resultCause)"
                    else ""
}