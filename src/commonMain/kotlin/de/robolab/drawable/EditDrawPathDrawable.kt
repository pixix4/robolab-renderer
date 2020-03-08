package de.robolab.drawable

import de.robolab.drawable.curve.BSpline
import de.robolab.drawable.curve.Curve
import de.robolab.drawable.utils.PathGenerator
import de.robolab.model.Direction
import de.robolab.renderer.DrawContext
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.IDrawable


class EditDrawPathDrawable(
        private val editPlanet: EditPlanetDrawable
) : IDrawable {

    private fun controlPoints(startPoint: Point, startDirection: Direction, endPoint: Point, endDirection: Direction): List<Point> {
        val linePoints = PathGenerator.generateControlPoints(startPoint, startDirection, endPoint, endDirection)
        return PathDrawable.linePointsToControlPoints(linePoints, startPoint, startDirection, endPoint, endDirection)
    }
    
    private fun calcDistance(startPoint: Point, endPoint: Point, controlPoints: List<Point>): Double =
            (listOf(startPoint) + controlPoints + endPoint).windowed(2, 1).sumByDouble { (p1, p2) ->
                p1.distance(p2)
            }

    private val curve: Curve = BSpline

    private fun multiEval(count: Int, startPoint: Point, endPoint: Point, controlPoints: List<Point>): List<Point> {
        val realCount = PathDrawable.power2(PathDrawable.log2(count - 1) + 1)

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

        points[index] = controlPoints.last()

        val startPointEdge = startPoint + (controlPoints.first() - startPoint).normalize() * PlottingConstraints.POINT_SIZE / 2
        val endPointEdge = endPoint + (controlPoints.last() - endPoint).normalize() * PlottingConstraints.POINT_SIZE / 2

        return listOf(startPointEdge) + points.take(index + 1).requireNoNulls() + endPointEdge
    }

    override fun onUpdate(ms_offset: Double): Boolean {
        return false
    }

    override fun onDraw(context: DrawContext) {
        val startEnd = editPlanet.interaction.startEnd ?: return
        val endEnd = editPlanet.pointer.findObjectUnderPointer<EditDrawEndDrawable.PointEnd>()

        val startPoint = Point(startEnd.point.first, startEnd.point.second)
        val startDirection = startEnd.direction

        if (endEnd == null) {
            val startPointEdge = startPoint + when (startDirection) {
                Direction.NORTH -> Point(0.0, PlottingConstraints.POINT_SIZE / 2)
                Direction.EAST -> Point(PlottingConstraints.POINT_SIZE / 2, 0.0)
                Direction.SOUTH -> Point(0.0, -PlottingConstraints.POINT_SIZE / 2)
                Direction.WEST -> Point(-PlottingConstraints.POINT_SIZE / 2, 0.0)
            }
            val endPoint = editPlanet.pointer.position

            context.strokeLine(listOf(startPointEdge, endPoint), context.theme.lineColor, PlottingConstraints.LINE_WIDTH)
        } else {
            val endPoint = Point(endEnd.point.first, endEnd.point.second)
            val endDirection = endEnd.direction

            val controlPoints = controlPoints(startPoint, startDirection, endPoint, endDirection)
            val distance = calcDistance(startPoint, endPoint, controlPoints)
            val steps = ((distance * context.transformation.scaledGridWidth) / 10).toInt()
            val p = multiEval(steps, startPoint, endPoint, controlPoints)
            context.strokeLine(p, context.theme.lineColor, PlottingConstraints.LINE_WIDTH)
        }
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        return emptyList()
    }
}
