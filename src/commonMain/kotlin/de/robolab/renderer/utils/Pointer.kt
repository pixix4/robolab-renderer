package de.robolab.renderer.utils

import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import kotlin.math.round

data class Pointer(
        val position: Point = Point.ZERO,
        val mousePosition: Point = Point.ZERO,
        val objectsUnderPointer: List<Any> = emptyList()
) {
    val roundedPosition = Point(
            round(position.left * PlottingConstraints.PRECISION_FACTOR) / PlottingConstraints.PRECISION_FACTOR,
            round(position.top * PlottingConstraints.PRECISION_FACTOR) / PlottingConstraints.PRECISION_FACTOR
    )

    inline fun <reified T : Any> findObjectUnderPointer(onlyTop: Boolean = false): T? {
        for (elem in objectsUnderPointer) {
            if (elem is T) {
                return elem
            }
            if (onlyTop) {
                return null
            }
        }

        return null
    }
}