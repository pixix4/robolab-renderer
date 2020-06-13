package de.robolab.client.renderer.drawable.utils

import de.robolab.common.utils.Point

/**
 * @author lars
 */
interface Curve {
    fun eval(t: Double, points: List<Point>): Point

    fun evalGradient(t: Double, points: List<Point>): Point {
        val epsilon = 0.01 / points.size

        val (p1, p2) = when {
            t < epsilon -> eval(t, points) to eval(t + epsilon, points)
            t > 1.0 - epsilon -> eval(t - epsilon, points) to eval(t, points)
            else -> eval(t - epsilon, points) to eval(t + epsilon, points)
        }

        if (p2 == p1) {
            val p3 = eval(t, points)

            if (p3 == p1) {
                return Point.ZERO
            }
            return (p2 - p3).normalize()
        }

        return (p2 - p1).normalize()
    }
}
