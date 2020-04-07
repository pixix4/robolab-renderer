package de.robolab.renderer.drawable.edit

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
import de.robolab.renderer.drawable.utils.Utils
import de.robolab.renderer.platform.ICanvas
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

    private var areSelectedControlPointsNull = false
    override fun onUpdate(ms_offset: Double): Boolean {
        val h = editPlanetDrawable.selectedPathControlPoints == null
        val changes = areSelectedControlPointsNull != h || !h
        areSelectedControlPointsNull = h
        return changes
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

    override fun onDraw(context: DrawContext) {
        val controlPoints = editPlanetDrawable.selectedPathControlPoints ?: return
        val path = editPlanetDrawable.selectedElement<Path>() ?: return
        
        val isOneWayPath = path.source == path.target && path.sourceDirection == path.targetDirection

        val first = controlPoints.first().let {
            Point(it.left.roundToInt(), it.top.roundToInt())
        }.interpolate(controlPoints.first(), 1.0 - (PlottingConstraints.POINT_SIZE / 2) / PlottingConstraints.CURVE_SECOND_POINT)
        val last = if (isOneWayPath) emptyList() else listOf(controlPoints.last().let {
            Point(it.left.roundToInt(), it.top.roundToInt())
        }.interpolate(controlPoints.last(), 1.0 - (PlottingConstraints.POINT_SIZE / 2) / PlottingConstraints.CURVE_SECOND_POINT))

        context.strokeLine(listOf(first) + controlPoints + last, context.theme.editColor, PlottingConstraints.LINE_WIDTH / 2)

        for ((i, point) in controlPoints.withIndex()) {
            if (i == 0 || (i == controlPoints.lastIndex && !isOneWayPath)) {
                continue
            }

            val divider = if (editPlanetDrawable.pointer.position.distance(point) < PlottingConstraints.POINT_SIZE / 2) 2 else 4

            context.fillArc(point, PlottingConstraints.POINT_SIZE / divider, 0.0, 2.0 * PI, context.theme.editColor)
            context.fillText(i.toString(), point, context.theme.primaryBackgroundColor, 4.0, alignment = ICanvas.FontAlignment.CENTER)
        }

        val p = editPlanetDrawable.pointer.findObjectUnderPointer<ControlPoint>() ?: return

        if (p.newPoint != null) {
            context.fillArc(p.newPoint, PlottingConstraints.POINT_SIZE / 4, 0.0, 2.0 * PI, context.theme.editColor)
            context.fillText("+${p.point}", p.newPoint, context.theme.primaryBackgroundColor, 4.0, alignment = ICanvas.FontAlignment.CENTER)
        }
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        if (!editPlanetDrawable.editable) return emptyList()

        val path = editPlanetDrawable.selectedElement<Path>() ?: return emptyList()
        val controlPoints = editPlanetDrawable.selectedPathControlPoints ?: return emptyList()
        val isOneWayPath = path.source == path.target && path.sourceDirection == path.targetDirection

        for ((i, point) in controlPoints.withIndex()) {
            if (i == 0 || (i == controlPoints.lastIndex && !isOneWayPath)) {
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
        val path = c.path
        val isOneWayPath = path.source == path.target && path.sourceDirection == path.targetDirection

        if (c.newPoint != null) {
            val allControlPoints = editPlanetDrawable.selectedPathControlPoints ?: return false
            val dropLast = if(isOneWayPath) 0 else 1
            val controlPoints = allControlPoints.drop(1).dropLast(dropLast).toMutableList()

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
        val path = editPlanetDrawable.selectedElement<Path>() ?: return false
        val isOneWayPath = path.source == path.target && path.sourceDirection == path.targetDirection

        val allControlPoints = editPlanetDrawable.selectedPathControlPoints ?: return false
        val dropLast = if(isOneWayPath) 0 else 1
        val oldControlPoints = allControlPoints.drop(1).dropLast(dropLast)
        val controlPoints = oldControlPoints.toMutableList()
        val index = indexP - 1

        if (index < 0 || index > controlPoints.lastIndex) return false

        controlPoints[index] = when {
            index == 0 -> Utils.calculateProjection(
                    editPlanetDrawable.pointer.position,
                    allControlPoints.first(),
                    path.sourceDirection
            )
            index == controlPoints.lastIndex && !isOneWayPath -> Utils.calculateProjection(
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
                val path = editPlanetDrawable.selectedElement<Path>() ?: return false
                val (_, indexP) = selectedControlPoint ?: return false
                val isOneWayPath = path.source == path.target && path.sourceDirection == path.targetDirection

                val allControlPoints = editPlanetDrawable.selectedPathControlPoints ?: return false
                val dropLast = if(isOneWayPath) 0 else 1
                val controlPoints = allControlPoints.drop(1).dropLast(dropLast).toMutableList()
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
