package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.drawable.base.Animatable
import de.robolab.client.renderer.drawable.edit.IEditCallback
import de.robolab.client.renderer.drawable.edit.PathEditManager
import de.robolab.client.renderer.drawable.utils.*
import de.robolab.client.renderer.events.KeyCode
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.renderer.view.base.menu
import de.robolab.client.renderer.view.component.*
import de.robolab.common.planet.*
import de.robolab.common.utils.Point
import de.westermann.kobserve.property.property
import kotlin.math.max
import kotlin.math.roundToInt


class PathAnimatable(
    reference: Path,
    planet: Planet,
    val editCallback: IEditCallback?
) : Animatable<Path>(reference) {

    override val view = SplineView(
        getSourcePointFromPath(reference),
        getTargetPointFromPath(planet.version, reference),
        getControlPointsFromPath(planet.version, reference),
        PlottingConstraints.LINE_WIDTH,
        getColor(planet, reference) ?: ViewColor.LINE_COLOR,
        reference.hidden
    )

    private val isWeightVisibleProperty = property(reference.weight?.let { it > 0.0 } ?: false)
    private val isBlockedProperty = property(reference.weight?.let { it < 0.0 } ?: false)
    private val isArrowVisibleProperty = property(reference.showDirectionArrow)

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
        val callback = editCallback ?: return@TextView false

        val number = if (newValue.isEmpty()) 1 else newValue.toIntOrNull() ?: return@TextView false

        callback.setPathWeight(reference, number)

        true
    }.also {
        it.animationTime = 0.0
    }

    private val blockedView = LineView(
        getOrthogonal(if (isOneWayPath && planet.version >= PlanetVersion.V2020_SPRING) 1.0 else 0.5, true).toList(),
        PlottingConstraints.LINE_WIDTH,
        ViewColor.POINT_RED
    ).also {
        it.animationTime = 0.0
    }

    private fun getArrowPosition(): Pair<Point, Point> {
        return if (isOneWayPath) {
            val (first, second) = getOrthogonal(1.0, false)

            val second2 = first + (first - second).orthogonal().normalize() * PlottingConstraints.ARROW_LENGTH
            first to second2
        } else {
            val (first, second) = getOrthogonal(0.5, false)
            val length = (second - first).orthogonal().normalize() * PlottingConstraints.ARROW_LENGTH / 2

            first - length to first + length
        }
    }

    private val arrowView = getArrowPosition().let { (first, second) ->
        ArrowView(
            first,
            second,
            PlottingConstraints.LINE_WIDTH * 0.65,
            ViewColor.LINE_COLOR
        )
    }.also {
        it.animationTime = 0.0
    }

    override fun onUpdate(obj: Path, planet: Planet) {
        super.onUpdate(obj, planet)

        view.setControlPoints(getControlPointsFromPath(planet.version, reference), 0.0)
        view.setSource(getSourcePointFromPath(reference), 0.0)
        view.setTarget(getTargetPointFromPath(planet.version, reference), 0.0)
        view.setColor(getColor(planet, reference) ?: ViewColor.LINE_COLOR)
        view.setIsDashed(reference.hidden)

        isWeightVisibleProperty.value = reference.weight?.let { it > 0.0 } ?: false
        isBlockedProperty.value = reference.weight?.let { it < 0.0 } ?: false
        isArrowVisibleProperty.value = reference.showDirectionArrow

        weightView.setSource(getOrthogonal(0.5, true).first)
        weightView.text = reference.weight?.toString() ?: "0"

        blockedView.setPoints(
            getOrthogonal(
                if (isOneWayPath && planet.version >= PlanetVersion.V2020_SPRING) 1.0 else 0.5,
                true
            ).toList()
        )

        val (first, second) = getArrowPosition()
        arrowView.setSource(first)
        arrowView.setTarget(second)

        pathEditManager?.onUpdate()
    }

    private var pathEditManager: PathEditManager? = null

    private val getOrCreateEditManager: PathEditManager
        get() {
            val currentManager = pathEditManager

            return if (currentManager == null) {
                val manager = PathEditManager(this, editCallback ?: throw IllegalStateException("PathEditManager cannot be created if not IEditCallback is present"))
                pathEditManager = manager
                manager
            } else currentManager
        }

    init {
        view += ConditionalView("Path weight", isWeightVisibleProperty, weightView)
        view += ConditionalView("Path blocked", isBlockedProperty, blockedView)
        view += ConditionalView("Path arrow", isArrowVisibleProperty, arrowView)

        view.focusable = true || editCallback != null
        weightView.focusable = editCallback != null

        if (editCallback != null ) {
            view.onFocus {
                view += getOrCreateEditManager.view
            }

            view.onBlur {
                view -= getOrCreateEditManager.view
            }
        }

        view.onPointerDown { event ->
            val callback = editCallback ?: return@onPointerDown
            val focusedView = view.document?.focusedStack?.lastOrNull() as? SquareView
            if (event.ctrlKey && focusedView != null) {
                val exposureCoordinate = Coordinate(
                    focusedView.center.left.roundToInt(),
                    focusedView.center.top.roundToInt()
                )

                callback.togglePathExposure(this.reference, exposureCoordinate)

                event.stopPropagation()
            }
        }

        view.onPointerSecondaryAction { event ->
            val callback = editCallback ?: return@onPointerSecondaryAction
            event.stopPropagation()

            val path = this.reference

            view.menu(
                event,
                "Path (${path.source.x}, ${path.source.y}, ${path.sourceDirection.name.first()}) -> (${path.target.x}, ${path.target.y}, ${path.targetDirection.name.first()})"
            ) {
                action("Reset control points") {
                    callback.updatePathControlPoints(path, emptyList())
                }

                action(if (path.hidden) "Mark path as visible" else "Mark path as hidden") {
                    callback.togglePathHiddenState(path)
                }

                if (path.weight != null) {
                    action(if (path.weight < 0) "Unblock path" else "Block path") {
                        callback.setPathWeight(path, if (path.weight < 0) 1 else -1)
                    }
                }

                if (path.exposure.isNotEmpty()) {
                    action("Remove path sender points") {
                        var grouping = false
                        for (exposure in path.exposure) {
                            callback.togglePathExposure(path, exposure, grouping)
                            grouping = true
                        }
                    }
                }

                action("Delete") {
                    callback.deletePath(path)
                }
            }
        }

        view.onKeyPress { event ->
            val callback = editCallback ?: return@onKeyPress

            when (event.keyCode) {
                KeyCode.DELETE, KeyCode.BACKSPACE -> {
                    callback.deletePath(this.reference)
                }
                else -> return@onKeyPress
            }

            event.stopPropagation()
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

        fun getTargetPointFromPath(version: PlanetVersion, path: Path): Point {
            if (path.isOneWayPath && version >= PlanetVersion.V2020_SPRING) {
                return getControlPointsFromPath(version, path).last()
            }

            return path.target.toPoint()
        }

        fun getControlPointsFromPath(
            version: PlanetVersion,
            path: Path
        ): List<Point> {
            return getControlPointsFromPath(
                version,
                path.source.toPoint(),
                path.sourceDirection,
                path.target.toPoint(),
                path.targetDirection,
                path.controlPoints
            )
        }

        private fun isPointInDirectLine(start: Point, direction: Direction, target: Point): Boolean = when (direction) {
            Direction.NORTH -> start.y <= target.y && start.x == target.x
            Direction.EAST -> start.x <= target.x && start.y == target.y
            Direction.SOUTH -> start.y >= target.y && start.x == target.x
            Direction.WEST -> start.x >= target.x && start.y == target.y
        }

        fun getControlPointsFromPath(
            version: PlanetVersion,
            startPoint: Point,
            startDirection: Direction,
            endPoint: Point,
            endDirection: Direction,
            controlPoints: List<Point> = emptyList()
        ): List<Point> {
            if (startPoint == endPoint && startDirection == endDirection && version >= PlanetVersion.V2020_SPRING) {
                val linePoints = if (controlPoints.isNotEmpty()) {
                    controlPoints
                } else {
                    listOf(startPoint.shift(startDirection, PlottingConstraints.CURVE_SECOND_POINT))
                }

                return listOfNotNull(
                    startPoint.shift(startDirection, PlottingConstraints.CURVE_FIRST_POINT),
                    if (linePoints.isNotEmpty() && isPointInDirectLine(
                            startPoint,
                            startDirection,
                            linePoints.first()
                        )
                    ) null else startPoint.shift(startDirection, PlottingConstraints.CURVE_SECOND_POINT),
                    *linePoints.toTypedArray()
                )
            }

            val linePoints = if (controlPoints.isNotEmpty()) {
                controlPoints
            } else {
                PathGenerator.generateControlPoints(
                    version,
                    startPoint,
                    startDirection,
                    endPoint,
                    endDirection
                )
            }

            return listOfNotNull(
                startPoint.shift(startDirection, PlottingConstraints.CURVE_FIRST_POINT),
                if (linePoints.isNotEmpty() && isPointInDirectLine(
                        startPoint,
                        startDirection,
                        linePoints.first()
                    )
                ) null else startPoint.shift(startDirection, PlottingConstraints.CURVE_SECOND_POINT),
                *linePoints.toTypedArray(),
                if (linePoints.isNotEmpty() && isPointInDirectLine(
                        endPoint,
                        endDirection,
                        linePoints.last()
                    )
                ) null else endPoint.shift(endDirection, PlottingConstraints.CURVE_SECOND_POINT),
                endPoint.shift(endDirection, PlottingConstraints.CURVE_FIRST_POINT)
            )
        }

        inline fun multiEval(
            count: Int,
            controlPoints: List<Point>,
            startPoint: Point,
            endPoint: Point?,
            eval: (Double) -> Point
        ): List<Point> {
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

            val startPointEdge =
                startPoint + (controlPoints.first() - startPoint).normalize() * PlottingConstraints.POINT_SIZE / 2

            if (endPoint == null) {
                return listOf(startPointEdge) + points.take(index + 1).requireNoNulls()
            }

            val endPointEdge =
                endPoint + (controlPoints.last() - endPoint).normalize() * PlottingConstraints.POINT_SIZE / 2
            return listOf(startPointEdge) + points.take(index + 1).requireNoNulls() + endPointEdge
        }

        fun evalLength(planetVersion: PlanetVersion, path: Path): Double {
            val controlPoints = getControlPointsFromPath(planetVersion, path)
            val lengthEstimate = path.length(controlPoints)
            val evalCount = (lengthEstimate * 10).roundToInt()

            val source = getSourcePointFromPath(path)
            val target = getTargetPointFromPath(planetVersion, path)

            val eval = listOf(source) + SplineView.evalSpline(
                evalCount,
                controlPoints,
                source,
                target,
                BSpline
            ) + target

            val factor = if (path.isOneWayPath && planetVersion < PlanetVersion.V2020_SPRING) 0.5 else 1.0

            return eval.windowed(2, 1)
                .sumByDouble { (p0, p1) -> p0.distanceTo(p1) } * factor
        }
    }
}
