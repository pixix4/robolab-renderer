package de.robolab.client.renderer.utils

import de.robolab.client.renderer.PlottingConstraints
import de.robolab.common.utils.Vector
import kotlin.math.round

data class Pointer(
    val position: Vector = Vector.ZERO,
    val mousePosition: Vector = Vector.ZERO
) {
    val roundedPosition = Vector(
            round(position.left * PlottingConstraints.PRECISION_FACTOR) / PlottingConstraints.PRECISION_FACTOR,
            round(position.top * PlottingConstraints.PRECISION_FACTOR) / PlottingConstraints.PRECISION_FACTOR
    )
}
