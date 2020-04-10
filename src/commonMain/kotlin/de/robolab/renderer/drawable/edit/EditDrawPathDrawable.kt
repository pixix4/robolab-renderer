package de.robolab.renderer.drawable.edit

import de.robolab.planet.Direction
import de.robolab.planet.Path
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.drawable.base.selectedElement
import de.robolab.renderer.drawable.general.PathAnimatable
import de.robolab.renderer.drawable.planet.EditPlanetDrawable
import de.robolab.renderer.drawable.utils.BSpline
import de.robolab.renderer.drawable.utils.Curve
import de.robolab.renderer.drawable.utils.PathGenerator
import de.robolab.renderer.drawable.utils.shift
import de.robolab.renderer.platform.KeyCode
import de.robolab.renderer.platform.KeyEvent
import de.robolab.renderer.utils.DrawContext


class EditDrawPathDrawable(
        private val editPlanetDrawable: EditPlanetDrawable
) : IDrawable {

    private fun controlPoints(startPoint: Point, startDirection: Direction, endPoint: Point, endDirection: Direction, controlPoints: List<Point>): List<Point> {
        return PathAnimatable.getControlPointsFromPath(startPoint, startDirection, endPoint, endDirection, controlPoints)
    }

    private fun calcDistance(startPoint: Point, endPoint: Point, controlPoints: List<Point>): Double =
            (listOf(startPoint) + controlPoints + endPoint).windowed(2, 1).sumByDouble { (p1, p2) ->
                p1.distance(p2)
            }

    private val curve: Curve = BSpline

    private fun multiEval(count: Int, startPoint: Point, endPoint: Point?, controlPoints: List<Point>): List<Point> {
        if (controlPoints.isEmpty()) {
            return listOfNotNull(startPoint, endPoint)
        }
        return PathAnimatable.multiEval(count, controlPoints, startPoint, endPoint) {
            curve.eval(it, controlPoints)
        }
    }

    override fun onUpdate(ms_offset: Double): Boolean {
        return editPlanetDrawable.selectedPointEnd != null
    }

    override fun onDraw(context: DrawContext) {
        val startEnd = editPlanetDrawable.selectedPointEnd ?: return
        val endEnd = editPlanetDrawable.pointer?.findObjectUnderPointer<EditDrawEndDrawable.PointEnd>()

        val startPoint = Point(startEnd.point)
        val startDirection = startEnd.direction

        val endPoint: Point
        val points: List<Point>
        if (endEnd == null) {
            endPoint = editPlanetDrawable.pointer?.position ?: return

            val lastPoint = editPlanetDrawable.createPathControlPoints.lastOrNull() ?: startPoint
            if (lastPoint.distance(endPoint) > 0.2) {
                editPlanetDrawable.createPathControlPoints = editPlanetDrawable.createPathControlPoints + endPoint
            }

            points = if (editPlanetDrawable.createPathWithCustomControlPoints) {
                val controlPoints = cleanControlPoints(
                        editPlanetDrawable.createPathControlPoints + endPoint,
                        startPoint, startDirection, null, null
                )
                val distance = calcDistance(startPoint, endPoint, controlPoints)
                val steps = ((distance * context.transformation.scaledGridWidth) / 10).toInt()
                multiEval(
                        steps,
                        startPoint,
                        null,
                        controlPoints
                ) + endPoint
            } else {
                listOf(startPoint.shift(startDirection, PlottingConstraints.POINT_SIZE / 2), endPoint)
            }
        } else {
            endPoint = Point(endEnd.point)
            val endDirection = endEnd.direction

            val controlPoints = if (editPlanetDrawable.createPathWithCustomControlPoints) {
                controlPoints(startPoint, startDirection, endPoint, endDirection, cleanControlPoints(
                        editPlanetDrawable.createPathControlPoints,
                        startPoint, startDirection, endPoint, endDirection
                ))
            } else {
                controlPoints(startPoint, startDirection, endPoint, endDirection, emptyList())
            }

            val distance = calcDistance(startPoint, endPoint, controlPoints)
            val steps = ((distance * context.transformation.scaledGridWidth) / 10).toInt()
            points = multiEval(
                    steps,
                    startPoint,
                    endPoint,
                    controlPoints
            ).toMutableList()
        }

        context.strokeLine(points, context.theme.lineColor, PlottingConstraints.LINE_WIDTH)
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        return emptyList()
    }

    override fun onKeyPress(event: KeyEvent): Boolean {
        if (!editPlanetDrawable.editable) return false

        when (event.keyCode) {
            KeyCode.DELETE -> {
                val path = editPlanetDrawable.selectedElement<Path>() ?: return false

                val cp = editPlanetDrawable.pointer?.findObjectUnderPointer<EditControlPointsDrawable.ControlPoint>()
                if (cp == null || cp.newPoint != null) {
                    editPlanetDrawable.editCallback.deletePath(path)
                }
            }
            KeyCode.ALT, KeyCode.CTRL -> {
                editPlanetDrawable.createPathWithCustomControlPoints = true
                return false
            }
            else -> {
                return false
            }
        }
        return true
    }
    
    override fun onKeyRelease(event: KeyEvent): Boolean {
        if (!editPlanetDrawable.editable) return false

        when (event.keyCode) {
            KeyCode.ALT, KeyCode.CTRL -> {
                editPlanetDrawable.createPathWithCustomControlPoints = false
            }
            else -> {
                return false
            }
        }
        return false
    }

    companion object {
        fun cleanControlPoints(controlPoints: List<Point>, startPoint: Point? = null, startDirection: Direction? = null, endPoint: Point? = null, endDirection: Direction? = null): List<Point> {
            val points = controlPoints.toMutableList()

            val firstPoint = points.firstOrNull()
            if (startPoint != null && firstPoint != null) {
                val ref = if (startDirection != null) {
                    startPoint.shift(startDirection, PlottingConstraints.CURVE_SECOND_POINT)
                } else startPoint
                
                if (firstPoint.distance(ref) < 0.2) {
                    points.removeAt(0)
                }

                if (startDirection != null) {
                    points.add(0, startPoint.shift(startDirection, PlottingConstraints.CURVE_FIRST_POINT))
                    if (!(points.isNotEmpty() && PathGenerator.isPointInDirectLine(startPoint, startDirection, points.first()))) {
                        points.add(1, startPoint.shift(startDirection, PlottingConstraints.CURVE_SECOND_POINT))
                    }
                }
            }
            val lastPoint = points.lastOrNull()
            if (endPoint != null && lastPoint != null) {
                val ref = if (endDirection != null) {
                    endPoint + endDirection.toVector() * PlottingConstraints.CURVE_SECOND_POINT
                } else endPoint

                if (lastPoint.distance(ref) < 0.2) {
                    points.removeAt(points.lastIndex)
                }
            }
            
            return points
        }
    }
}
