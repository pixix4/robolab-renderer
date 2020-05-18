package de.robolab.renderer.drawable.utils

import de.robolab.renderer.data.Point
import kotlin.math.min

/**
 * @author lars
 */
object BSpline : Curve {
    override fun eval(t: Double, points: List<Point>): Point =
            eval(t, min(DEFAULT_DEGREE, points.lastIndex), points)

    //override fun evalGradient(t: Double, points: List<Point>): Point =
    //        eval(t, min(DEFAULT_DEGREE, points.lastIndex) - 1, points).normalize()

    private fun eval(t: Double, degree: Int, points: List<Point>): Point {
        if (t == 0.0) return points.first()
        if (t == 1.0) return points.last()
        val v = vector(degree, points.size)
        return coxDeBoor(
                v.indexOfLast {
                    it <= (t * (points.size - degree))
                },
                t * (points.size - degree),
                v,
                points,
                degree
        )
    }

    private val vectorCache: MutableMap<Pair<Int, Int>, List<Int>> = mutableMapOf()
    private fun vector(degree: Int, pointCount: Int): List<Int> {
        return vectorCache.getOrPut(degree to pointCount) {
            val list = mutableListOf<Int>()
            for (i in 0 until degree)
                list += 0
            for (i in 0..(pointCount - degree))
                list += i
            for (i in 0 until degree)
                list += pointCount - degree
            list
        }
    }

    private fun coxDeBoor(curveSegment: Int, t: Double, vector: List<Int>, points: List<Point>, degree: Int): Point {
        val d: MutableList<Point> = ArrayList(degree + 1)
        for (index in 0..degree) {
            d += points[index + curveSegment - degree]
        }
        for (r in 1..degree) {
            for (j in degree downTo r) {
                val alpha = (t - vector[j + curveSegment - degree]) /
                        (vector[j + 1 + curveSegment - r] - vector[j + curveSegment - degree])
                d[j] = (d[j - 1] * (1.0 - alpha)) + (d[j] * alpha)
            }
        }
        return d[degree]
    }

    private const val DEFAULT_DEGREE = 3
}
