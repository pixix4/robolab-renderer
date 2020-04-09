package de.robolab.planet

import de.robolab.planet.Coordinate
import de.robolab.planet.Direction
import de.robolab.planet.Path
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
