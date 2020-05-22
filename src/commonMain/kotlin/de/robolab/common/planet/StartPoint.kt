package de.robolab.common.planet

import de.robolab.common.utils.Point

data class StartPoint(
    val point: Coordinate,
    val orientation: Direction,
    val controlPoints: List<Point>
) {
    val path = Path(
        point,
        orientation.opposite(),
        point,
        orientation.opposite(),
        null,
        emptySet(),
        controlPoints,
        false
    )

    fun equalPoint(other: StartPoint) = point == other.point && orientation == other.orientation
}
