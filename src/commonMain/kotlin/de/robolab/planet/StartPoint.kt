package de.robolab.model

import de.robolab.renderer.data.Point

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
}
