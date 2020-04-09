package de.robolab.planet

import de.robolab.planet.Coordinate
import de.robolab.planet.Direction

data class PathSelect(
        val point: Coordinate,
        val direction: Direction
)