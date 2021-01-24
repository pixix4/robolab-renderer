package de.robolab.client.renderer.drawable.utils

import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.utils.removeConsecutiveDuplicateIf
import de.robolab.common.utils.Point
import kotlin.math.max

data class CurveEval(
    val point: Point,
    val curveLength: Double,
    val curveProgress: Double
) {
    companion object {
        fun evalSpline(
            count: Int,
            controlPoints: List<Point>,
            source: Point,
            target: Point,
            curve: Curve,
            improveCount: Boolean = true
        ): List<Point> {
            val realCount = if (improveCount) {
                max(16, power2(log2(count - 1) + 1))
            } else count

            val points = arrayOfNulls<Point>(realCount + 1)

            val step = 1.0 / realCount
            var t = 2 * step

            points[0] = controlPoints.first()

            var index = 1
            while (t < 1.0) {
                points[index] = curve.eval(t - step, controlPoints)
                t += step
                index += 1
            }

            points[index] = (controlPoints.last())

            val startPointEdge =
                source + (controlPoints.first() - source).normalize() * PlottingConstraints.POINT_SIZE / 2
            val endPointEdge = target + (controlPoints.last() - target).normalize() * PlottingConstraints.POINT_SIZE / 2

            val pointList = points.take(index + 1).requireNoNulls()

            return (if (startPointEdge == pointList.firstOrNull()) {
                if (endPointEdge == pointList.lastOrNull()) {
                    pointList
                } else {
                    pointList + endPointEdge
                }
            } else {
                if (endPointEdge == pointList.lastOrNull()) {
                    listOf(startPointEdge) + pointList
                } else {
                    listOf(startPointEdge) + pointList + endPointEdge
                }
            }).removeConsecutiveDuplicateIf { a, b ->
                a.distanceTo(b) < 1e-4
            }
        }


        fun evalSplineAttributed(
            count: Int,
            controlPoints: List<Point>,
            source: Point,
            target: Point,
            curve: Curve,
            improveCount: Boolean = true
        ): List<CurveEval> {
            val points = evalSpline(count, controlPoints, source, target, curve, improveCount)

            val lengthArray = Array(points.size) { 0.0 }
            for (i in 1 until points.size) {
                lengthArray[i] = lengthArray[i - 1] + points[i - 1].distanceTo(points[i])
            }
            val length = lengthArray[lengthArray.size - 1]

            return Array(points.size) { i ->
                CurveEval(
                    points[i],
                    lengthArray[i],
                    lengthArray[i] / length
                )
            }.asList()
        }
    }
}
