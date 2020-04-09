package de.robolab.planet

import de.robolab.renderer.data.Point

data class Path(
        val source: Coordinate,
        val sourceDirection: Direction,
        val target: Coordinate,
        val targetDirection: Direction,
        val weight: Int?,
        val exposure: Set<Coordinate>,
        val controlPoints: List<Point>,
        val hidden: Boolean
) {

    val blocked: Boolean
    get() = weight != null && weight < 0

    fun equalPath(other: Path): Boolean {
        val thisP0 = source to sourceDirection
        val thisP1 = target to targetDirection

        val otherP0 = other.source to other.sourceDirection
        val otherP1 = other.target to other.targetDirection

        return thisP0 == otherP0 && thisP1 == otherP1 || thisP0 == otherP1 && thisP1 == otherP0
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
}
