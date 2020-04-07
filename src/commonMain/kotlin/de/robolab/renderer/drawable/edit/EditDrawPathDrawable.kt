package de.robolab.renderer.drawable.edit

import de.robolab.model.Direction
import de.robolab.model.Path
import de.robolab.renderer.utils.DrawContext
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.general.PathAnimatable
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.drawable.base.selectedElement
import de.robolab.renderer.drawable.planet.EditPlanetDrawable
import de.robolab.renderer.drawable.utils.BSpline
import de.robolab.renderer.drawable.utils.Curve
import de.robolab.renderer.platform.KeyCode
import de.robolab.renderer.platform.KeyEvent


class EditDrawPathDrawable(
        private val editPlanetDrawable: EditPlanetDrawable
) : IDrawable {

    private fun controlPoints(startPoint: Point, startDirection: Direction, endPoint: Point, endDirection: Direction): List<Point> {
        return PathAnimatable.getControlPointsFromPath(startPoint, startDirection, endPoint, endDirection)
    }

    private fun calcDistance(startPoint: Point, endPoint: Point, controlPoints: List<Point>): Double =
            (listOf(startPoint) + controlPoints + endPoint).windowed(2, 1).sumByDouble { (p1, p2) ->
                p1.distance(p2)
            }

    private val curve: Curve = BSpline

    private fun multiEval(count: Int, startPoint: Point, endPoint: Point, controlPoints: List<Point>): List<Point> {
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

        if (endEnd == null) {
            val startPointEdge = startPoint + startDirection.toVector() * (PlottingConstraints.POINT_SIZE / 2)
            val endPoint = editPlanetDrawable.pointer?.position

            if (endPoint != null) {
                context.strokeLine(listOf(startPointEdge, endPoint), context.theme.lineColor, PlottingConstraints.LINE_WIDTH)
            }
        } else {
            val endPoint = Point(endEnd.point)
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
            else -> {
                return false
            }
        }
        return true
    }
}
