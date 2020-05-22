package de.robolab.client.renderer.utils

import de.robolab.client.renderer.PlottingConstraints
import de.robolab.common.utils.Point
import kotlin.math.round

data class Pointer(
    val position: Point = Point.ZERO,
    val mousePosition: Point = Point.ZERO
) {
    val roundedPosition = Point(
            round(position.left * PlottingConstraints.PRECISION_FACTOR) / PlottingConstraints.PRECISION_FACTOR,
            round(position.top * PlottingConstraints.PRECISION_FACTOR) / PlottingConstraints.PRECISION_FACTOR
    )
}
