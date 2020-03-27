package de.robolab.renderer.drawable.utils

import de.robolab.renderer.data.Point
import kotlin.math.pow

/**
 * @author lars
 */
object Bezier : Curve {
    override fun eval(t: Double, points: List<Point>): Point = when (points.size) {
        1 -> points[0]
        2 -> linearBezier(t, points[0], points[1])
        3 -> quadraticBezier(t, points[0], points[1], points[2])
        4 -> cubicBezier(t, points[0], points[1], points[2], points[3])
        else -> genericBezier(t, points)
    }

    private fun linearBezier(t: Double, p0: Point, p1: Point): Point =
            p0 * (1 - t) +
                    p1 * t

    private fun quadraticBezier(t: Double, p0: Point, p1: Point, p2: Point): Point =
            (p0 - p1 * 2 + p2) * t.pow(2) +
                    (-p0 * 2 + p1 * 2) * t +
                    p0


    private fun cubicBezier(t: Double, p0: Point, p1: Point, p2: Point, p3: Point): Point =
            (-p0 + p1 * 3 - p2 * 3 + p3) * t.pow(3) +
                    (p0 * 3 - p1 * 6 + p2 * 3) * t.pow(2) +
                    (p0 * -3 + p1 * 3) * t +
                    p0

    private fun bernsteinPolynomial(i: Int, n: Int, t: Double): Double =
            (n over i) * t.pow(i) * (1 - t).pow(n - i)

    private fun genericBezier(t: Double, points: List<Point>): Point =
            points.foldIndexed(Point.ZERO) { index, acc, point ->
                acc + point * bernsteinPolynomial(index, points.size, t)
            }

    private infix fun Int.over(k: Int): Int {
        val k1 = if (2 * k > this) this - k else k
        if (k1 == 0) return 1
        if (k1 < 0) throw IllegalArgumentException("Cannot calc ($this over $k)!")

        var result = 1

        for (i in 1..(k1 + 1)) {
            result *= (this - k1 + i) / i
        }

        return result
    }
}
