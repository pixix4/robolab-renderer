package de.robolab.renderer.drawable.general

import de.robolab.planet.Direction
import de.robolab.planet.Path
import de.robolab.planet.Planet
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import de.robolab.renderer.document.*
import de.robolab.renderer.drawable.base.Animatable
import de.robolab.renderer.drawable.edit.IEditCallback
import de.robolab.renderer.drawable.edit.PathEditManager
import de.robolab.renderer.drawable.utils.*
import de.robolab.renderer.platform.ICanvas
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.property
import kotlin.math.max


class PathAnimatable(
        reference: Path,
        planet: Planet,
        val editProperty: ObservableValue<IEditCallback?>
) : Animatable<Path>(reference) {

    override val view = SplineView(
            getSourcePointFromPath(reference),
            getTargetPointFromPath(reference),
            getControlPointsFromPath(reference),
            PlottingConstraints.LINE_WIDTH,
            getColor(planet, reference) ?: ViewColor.LINE_COLOR,
            reference.hidden
    )

    private val isWeightVisibleProperty = property(reference.weight?.let { it > 0.0 } ?: false)
    private val isBlockedProperty = property(reference.weight?.let { it < 0.0 } ?: false)

    val isOneWayPath
        get() = reference.source == reference.target && reference.sourceDirection == reference.targetDirection

    private fun getOrthogonal(t: Double, swap: Boolean): Pair<Point, Point> {
        val position = view.eval(t)
        val gradient = view.evalGradient(t)

        val vec = gradient.orthogonal()

        var source = position + vec * 0.1
        var target = position - vec * 0.1

        if (source > target && swap) {
            val h = target
            target = source
            source = h
        }

        return source to target
    }

    private val weightView = TextView(
            getOrthogonal(0.5, true).first,
            12.0,
            reference.weight?.toString() ?: "0",
            ViewColor.LINE_COLOR,
            ICanvas.FontAlignment.CENTER,
            ICanvas.FontWeight.NORMAL
    ) { newValue ->
        val callback = editProperty.value ?: return@TextView false
        
        val number = if (newValue.isEmpty()) 0 else newValue.toIntOrNull() ?: return@TextView false
        
        callback.setPathWeight(reference, number)

        true
    }.also {
        it.animationTime = 0.0
    }

    private val blockedView = LineView(
            getOrthogonal(if (isOneWayPath) 1.0 else 0.5, true).first,
            getOrthogonal(if (isOneWayPath) 1.0 else 0.5, true).second,
            PlottingConstraints.LINE_WIDTH,
            ViewColor.POINT_RED
    ).also {
        it.animationTime = 0.0
    }

    override fun onUpdate(obj: Path, planet: Planet) {
        super.onUpdate(obj, planet)

        view.setControlPoints(getControlPointsFromPath(reference), 0.0)
        view.setSource(getSourcePointFromPath(reference), 0.0)
        view.setTarget(getTargetPointFromPath(reference), 0.0)
        view.setColor(getColor(planet, reference) ?: ViewColor.LINE_COLOR)
        view.setIsDashed(reference.hidden)


        isBlockedProperty.value = reference.weight?.let { it < 0.0 } ?: false

        weightView.setCenter(getOrthogonal(0.5, true).first)
        weightView.text = reference.weight?.toString() ?: "0"

        blockedView.setSource(getOrthogonal(if (isOneWayPath) 1.0 else 0.5, true).first)
        blockedView.setTarget(getOrthogonal(if (isOneWayPath) 1.0 else 0.5, true).second)

        pathEditManager?.onUpdate()
    }

    private var pathEditManager: PathEditManager? = null

    private val getOrCreateEditManager: PathEditManager
        get() {
            val currentManager = pathEditManager

            return if (currentManager == null) {
                val manager = PathEditManager(this)
                pathEditManager = manager
                manager
            } else currentManager
        }

    init {
        view += ConditionalView("Path weight",isWeightVisibleProperty, weightView)
        view += ConditionalView("Path blocked", isBlockedProperty, blockedView)

        view.focusable = editProperty.value != null
        weightView.focusable = editProperty.value != null
        editProperty.onChange {
            view.focusable = editProperty.value != null
            weightView.focusable = editProperty.value != null
        }

        view.onFocus {
            view += getOrCreateEditManager.view
        }

        view.onBlur {
            view -= getOrCreateEditManager.view
        }
    }

    private fun getColor(planet: Planet, path: Path): ViewColor? {
        if (path.exposure.isEmpty()) {
            return null
        }
        return ViewColor.c(Utils.getColorByIndex(Utils.getSenderGrouping(planet).getValue(path.exposure)))
    }

    companion object {
        fun getSourcePointFromPath(path: Path): Point {
            return path.source.toPoint()
        }

        fun getTargetPointFromPath(path: Path): Point {
            if (path.source == path.target && path.sourceDirection == path.targetDirection) {
                return getControlPointsFromPath(path).last()
            }

            return path.target.toPoint()
        }

        fun getControlPointsFromPath(path: Path): List<Point> {
            return getControlPointsFromPath(
                    path.source.toPoint(),
                    path.sourceDirection,
                    path.target.toPoint(),
                    path.targetDirection,
                    path.controlPoints
            )
        }

        fun getControlPointsFromPath(
                startPoint: Point,
                startDirection: Direction,
                endPoint: Point,
                endDirection: Direction,
                controlPoints: List<Point> = emptyList()
        ): List<Point> {
            if (startPoint == endPoint && startDirection == endDirection) {
                val linePoints = if (controlPoints.isNotEmpty()) {
                    controlPoints
                } else {
                    listOf(startPoint.shift(startDirection, PlottingConstraints.CURVE_SECOND_POINT))
                }

                return listOfNotNull(
                        startPoint.shift(startDirection, PlottingConstraints.CURVE_FIRST_POINT),
                        if (linePoints.isNotEmpty() && PathGenerator.isPointInDirectLine(startPoint, startDirection, linePoints.first())) null else startPoint.shift(startDirection, PlottingConstraints.CURVE_SECOND_POINT),
                        *linePoints.toTypedArray()
                )
            }

            val linePoints = if (controlPoints.isNotEmpty()) {
                controlPoints
            } else {
                PathGenerator.generateControlPoints(
                        startPoint,
                        startDirection,
                        endPoint,
                        endDirection
                )
            }

            return listOfNotNull(
                    startPoint.shift(startDirection, PlottingConstraints.CURVE_FIRST_POINT),
                    if (linePoints.isNotEmpty() && PathGenerator.isPointInDirectLine(startPoint, startDirection, linePoints.first())) null else startPoint.shift(startDirection, PlottingConstraints.CURVE_SECOND_POINT),
                    *linePoints.toTypedArray(),
                    if (linePoints.isNotEmpty() && PathGenerator.isPointInDirectLine(endPoint, endDirection, linePoints.last())) null else endPoint.shift(endDirection, PlottingConstraints.CURVE_SECOND_POINT),
                    endPoint.shift(endDirection, PlottingConstraints.CURVE_FIRST_POINT)
            )
        }

        inline fun multiEval(count: Int, controlPoints: List<Point>, startPoint: Point, endPoint: Point?, eval: (Double) -> Point): List<Point> {
            val realCount = max(16, power2(log2(count - 1) + 1))

            val points = arrayOfNulls<Point>(realCount + 1)

            val step = 1.0 / realCount
            var t = 2 * step

            points[0] = controlPoints.first()

            var index = 1
            while (t < 1.0) {
                points[index] = eval(t - step)
                t += step
                index += 1
            }

            points[index] = (controlPoints.last())

            val startPointEdge = startPoint + (controlPoints.first() - startPoint).normalize() * PlottingConstraints.POINT_SIZE / 2

            if (endPoint == null) {
                return listOf(startPointEdge) + points.take(index + 1).requireNoNulls()
            }

            val endPointEdge = endPoint + (controlPoints.last() - endPoint).normalize() * PlottingConstraints.POINT_SIZE / 2
            return listOf(startPointEdge) + points.take(index + 1).requireNoNulls() + endPointEdge
        }
    }
}
