package de.robolab.renderer.drawable.utils

import de.robolab.renderer.data.Point

/**
 * @author lars
 */
interface Curve {
    fun eval(t: Double, points: List<Point>): Point

    fun eval(t: Double, degree: Int, points: List<Point>): Point {
        throw NotImplementedError("Curve of type '${this::class.simpleName}' does not support a custom degree!")
    }

    fun evalGradient(t: Double, points: List<Point>): Point {
        val epsilon = 0.01 / points.size

        val (p1, p2) = when {
            t < epsilon -> eval(t, points) to eval(t + epsilon, points)
            t > 1.0 - epsilon -> eval(t - epsilon, points) to eval(t, points)
            else -> eval(t - epsilon, points) to eval(t + epsilon, points)
        }

        return (p2 - p1).normalize()
    }
}
