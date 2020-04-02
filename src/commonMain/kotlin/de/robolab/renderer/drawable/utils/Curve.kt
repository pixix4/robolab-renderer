package de.robolab.renderer.drawable.utils

import de.robolab.renderer.data.Point

/**
 * @author lars
 */
interface Curve {
    fun eval(t: Double, points: List<Point>): Point

    fun eval(t: Double, degree: Int, points: List<Point>): Point {
        throw NotImplementedError("Curve of type does not support a custom degree!")
    }

    fun evalD(t: Double, points: List<Point>): Point {
        val (p1, p2) = when {
            t < 0.01 -> eval(t, points) to eval(t + 0.01, points)
            t > 0.99 -> eval(t - 0.01, points) to eval(t, points)
            else -> eval(t - 0.01, points) to eval(t + 0.01, points)
        }

        return (p2 - p1).normalize()
    }
}
