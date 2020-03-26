package de.robolab.renderer.drawable.edit

import de.robolab.model.Path
import de.robolab.renderer.DrawContext
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.PathDrawable
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.drawable.curve.BSpline
import de.robolab.renderer.drawable.curve.Curve
import de.robolab.renderer.drawable.utils.Utils
import de.robolab.renderer.platform.KeyCode
import de.robolab.renderer.platform.KeyEvent
import de.robolab.renderer.platform.PointerEvent
import kotlin.math.PI
import kotlin.math.round
import kotlin.math.roundToInt

class EditControlPointsDrawable(
        private val editPlanetDrawable: EditPlanetDrawable
) : IDrawable {

    data class ControlPoint(
            val path: Path,
            val point: Int,
            val newPoint: Point? = null
    )

    private var areControlPointsNull = false
    override fun onUpdate(ms_offset: Double): Boolean {
        val h = editPlanetDrawable.selectedPathControlPoints == null
        val changes = areControlPointsNull != h || !h
        areControlPointsNull = h
        return changes
    }

    private fun calcDistance(startPoint: Point, endPoint: Point, controlPoints: List<Point>): Double =
            (listOf(startPoint) + controlPoints + endPoint).windowed(2, 1).sumByDouble { (p1, p2) ->
                p1.distance(p2)
            }

    private val curve: Curve = BSpline

    private fun multiEval(count: Int, startPoint: Point, endPoint: Point, controlPoints: List<Point>): List<Point> {
        return PathDrawable.multiEval(count, controlPoints, startPoint, endPoint) {
            curve.eval(it, controlPoints)
        }
    }

    override fun onDraw(context: DrawContext) {
        val controlPoints = editPlanetDrawable.selectedPathControlPoints ?: return

        val first = controlPoints.first().let {
            Point(it.left.roundToInt(), it.top.roundToInt())
        }.interpolate(controlPoints.first(), 1.0 - (PlottingConstraints.POINT_SIZE / 2) / PlottingConstraints.CURVE_SECOND_POINT)
        val last = controlPoints.last().let {
            Point(it.left.roundToInt(), it.top.roundToInt())
        }.interpolate(controlPoints.last(), 1.0 - (PlottingConstraints.POINT_SIZE / 2) / PlottingConstraints.CURVE_SECOND_POINT)

        context.strokeLine(listOf(first) + controlPoints + last, context.theme.editColor, PlottingConstraints.LINE_WIDTH / 2)

        for ((i, point) in controlPoints.withIndex()) {
            if (i == 0 || i == controlPoints.size - 1) {
                continue
            }

            val divider = if (editPlanetDrawable.pointer.position.distance(point) < PlottingConstraints.POINT_SIZE / 2) 2 else 4

            context.fillArc(point, PlottingConstraints.POINT_SIZE / divider, 0.0, 2.0 * PI, context.theme.editColor)
            context.fillText(i.toString(), point, context.theme.primaryBackgroundColor, 4.0)
        }

        val p = editPlanetDrawable.pointer.findObjectUnderPointer<ControlPoint>() ?: return

        if (p.newPoint != null) {
            context.fillArc(p.newPoint, PlottingConstraints.POINT_SIZE / 4, 0.0, 2.0 * PI, context.theme.editColor)
            context.fillText("+${p.point}", p.newPoint, context.theme.primaryBackgroundColor, 4.0)
        }
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        if (!editPlanetDrawable.editable) return emptyList()

        val path = editPlanetDrawable.selectedPath ?: return emptyList()
        val controlPoints = editPlanetDrawable.selectedPathControlPoints ?: return emptyList()

        for ((i, point) in controlPoints.withIndex()) {
            if (i == 0 || i == controlPoints.size - 1) {
                continue
            }

            if (editPlanetDrawable.pointer.position.distance(point) < PlottingConstraints.POINT_SIZE / 2) {
                return listOf(ControlPoint(
                        path, i
                ))
            }
        }

        val startPoint = Point(path.source)
        val endPoint = Point(path.target)

        val distance = calcDistance(startPoint, endPoint, controlPoints)
        val steps = ((distance * context.transformation.scaledGridWidth) / 10).toInt()
        val p = multiEval(steps, startPoint, endPoint, controlPoints).mapIndexed { index, point ->
            Triple(point, index, point.distance(editPlanetDrawable.pointer.position))
        }

        val (minPoint, minIndex, minDist) = p.minBy { it.third } ?: return emptyList()

        if (minDist < PlottingConstraints.POINT_SIZE / 2) {
            val i = ((controlPoints.size - 3) * (minIndex.toDouble() / p.size.toDouble())).toInt() + 2
            return listOf(ControlPoint(
                    path, i, minPoint
            ))
        }

        return emptyList()
    }


    private var selectedControlPoint: ControlPoint? = null
    private var groupHistory = false

    override fun onPointerDown(event: PointerEvent): Boolean {
        if (!editPlanetDrawable.editable) return false

        selectedControlPoint = null
        val c = editPlanetDrawable.pointer.findObjectUnderPointer<ControlPoint>() ?: return false

        if (c.newPoint != null) {
            val allControlPoints = editPlanetDrawable.selectedPathControlPoints ?: return false
            val controlPoints = allControlPoints.drop(1).dropLast(1).toMutableList()

            controlPoints.add(c.point - 1, c.newPoint)
            editPlanetDrawable.editCallback.updatePathControlPoints(c.path, controlPoints, false)
        }

        selectedControlPoint = c
        groupHistory = false

        return true

    }

    override fun onPointerDrag(event: PointerEvent): Boolean {
        if (!editPlanetDrawable.editable) return false

        val (_, indexP) = selectedControlPoint ?: return false
        val path = editPlanetDrawable.selectedPath ?: return false

        val allControlPoints = editPlanetDrawable.selectedPathControlPoints ?: return false
        val oldControlPoints = allControlPoints.drop(1).dropLast(1)
        val controlPoints = oldControlPoints.toMutableList()
        val index = indexP - 1

        if (index < 0 || index > controlPoints.lastIndex) return false

        controlPoints[index] = when (index) {
            0 -> Utils.calculateProjection(
                    editPlanetDrawable.pointer.position,
                    allControlPoints.first(),
                    path.sourceDirection
            )
            controlPoints.lastIndex -> Utils.calculateProjection(
                    editPlanetDrawable.pointer.position,
                    allControlPoints.last(),
                    path.targetDirection
            )
            else -> editPlanetDrawable.pointer.position
        }.let {
            Point(
                    round(it.left * PlottingConstraints.PRECISION_FACTOR) / PlottingConstraints.PRECISION_FACTOR,
                    round(it.top * PlottingConstraints.PRECISION_FACTOR) / PlottingConstraints.PRECISION_FACTOR
            )
        }

        if (oldControlPoints != controlPoints) {
            editPlanetDrawable.editCallback.updatePathControlPoints(path, controlPoints, groupHistory)
            groupHistory = true
        }

        return true
    }

    override fun onKeyPress(event: KeyEvent): Boolean {
        if (!editPlanetDrawable.editable) return false

        when (event.keyCode) {
            KeyCode.DELETE -> {
                val path = editPlanetDrawable.selectedPath ?: return false
                val (_, indexP) = selectedControlPoint ?: return false

                val allControlPoints = editPlanetDrawable.selectedPathControlPoints ?: return false
                val controlPoints = allControlPoints.drop(1).dropLast(1).toMutableList()
                val index = indexP - 1

                if (index <= 0 || index >= controlPoints.lastIndex) return false

                controlPoints.removeAt(index)

                editPlanetDrawable.editCallback.updatePathControlPoints(path, controlPoints, false)
                selectedControlPoint = null
            }
            else -> {
                return false
            }
        }
        return true
    }
}
