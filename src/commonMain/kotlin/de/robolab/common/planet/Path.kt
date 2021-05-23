package de.robolab.common.planet

import de.robolab.client.renderer.drawable.utils.toPoint
import de.robolab.common.utils.Point
import kotlin.math.roundToInt

data class Path(
    val source: Coordinate,
    val sourceDirection: Direction,
    val target: Coordinate,
    val targetDirection: Direction,
    val weight: Int?,
    val exposure: Set<Coordinate>,
    val controlPoints: List<Point>,
    val hidden: Boolean,
    val showDirectionArrow: Boolean
): IPlanetValue {

    val blocked: Boolean
        get() = weight != null && weight < 0

    fun length(controlPoints: List<Point> = this.controlPoints): Double {
        return (listOf(Point(source.x, source.y)) + controlPoints + Point(target.x, target.y)).windowed(2, 1)
            .sumOf { (p0, p1) -> p0.distanceTo(p1) }
    }

    val isOneWayPath: Boolean
        get() = source == target && sourceDirection == targetDirection

    fun equalPath(other: Path): Boolean {
        val thisP0 = source to sourceDirection
        val thisP1 = target to targetDirection

        val otherP0 = other.source to other.sourceDirection
        val otherP1 = other.target to other.targetDirection

        return thisP0 == otherP0 && thisP1 == otherP1 || thisP0 == otherP1 && thisP1 == otherP0
    }

    fun connectsWith(point: Coordinate): Boolean {
        return source == point || target == point
    }

    fun connectsWith(point: Coordinate, direction: Direction): Boolean {
        return source == point && direction == sourceDirection || target == point && targetDirection == direction
    }

    fun reversed(): Path {
        return copy(
            source = target,
            sourceDirection = targetDirection,
            target = source,
            targetDirection = sourceDirection,
            controlPoints = controlPoints.reversed()
        )
    }

    fun translate(delta: Coordinate) = Path(
        source.translate(delta),
        sourceDirection,
        target.translate(delta),
        targetDirection,
        weight,
        exposure.map { it.translate(delta) }.toSet(),
        controlPoints.map { it + delta.toPoint() },
        hidden,
        showDirectionArrow
    )

    fun rotate(direction: Planet.RotateDirection, origin: Coordinate) = Path(
        source.rotate(direction, origin),
        sourceDirection.rotate(direction),
        target.rotate(direction, origin),
        targetDirection.rotate(direction),
        weight,
        exposure.map { it.rotate(direction, origin) }.toSet(),
        controlPoints.map { it.rotate(direction.angle, origin.toPoint()) },
        hidden,
        showDirectionArrow
    )

    fun scaleWeights(factor: Double = 1.0, offset: Int = 0): Path {
        if (weight == null) return this

        val scaled = if (weight > 0) (weight * factor).roundToInt() + offset else weight
        val w = if (scaled < 0) -1 else scaled
        return copy(weight = w)
    }

    override fun toString(): String = "Path(" +
            "${source.x},${source.y},${sourceDirection.letter()} " +
            "${target.x},${target.y},${targetDirection.letter()} $weight " +
            exposure.joinToString(" ") { "${it.x},${it.y}" } +
            if (blocked) if (exposure.isEmpty()) "blocked)" else " blocked)" else ")"
}
