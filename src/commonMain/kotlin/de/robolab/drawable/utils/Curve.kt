package de.robolab.drawable.curve

import de.robolab.renderer.data.Point

/**
 * @author lars
 */
interface Curve {
    fun eval(t: Double, points: List<Point>): Point

    fun eval(t: Double, degree: Int, points: List<Point>): Point {
        throw NotImplementedError("Curve of type does not support a custom degree!")
    }
}
