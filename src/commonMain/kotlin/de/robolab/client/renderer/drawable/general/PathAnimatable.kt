package de.robolab.client.renderer.drawable.general

import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.drawable.base.Animatable
import de.robolab.client.renderer.drawable.edit.IEditCallback
import de.robolab.client.renderer.drawable.edit.PathEditManager
import de.robolab.client.renderer.drawable.utils.*
import de.robolab.client.renderer.events.KeyCode
import de.robolab.client.renderer.events.PointerEvent
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.renderer.view.base.menu
import de.robolab.client.renderer.view.component.*
import de.robolab.client.utils.PathClassification
import de.robolab.common.planet.*
import de.robolab.common.planet.utils.PlanetVersion
import de.robolab.common.utils.Vector
import de.westermann.kobserve.property.property
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.roundToLong


class PathAnimatable(
    reference: PlanetPath,
    planet: Planet,
    val editCallback: IEditCallback?,
) : Animatable<PlanetPath>(reference) {

    private fun generateHighlightColors(planet: Planet, reference: PlanetPath): List<SplineView.Color> {
        val segments = PathClassification.classify(planet.version, reference)?.explicitSegments ?: emptyList()

        return segments.map { segment ->
            val percent = min(1.5, segment.curviness / 500.0) * 1.5

            val highlight = if (percent < 0.5) {
                ViewColor.HIGHLIGHT_COLOR.interpolate(ViewColor.PRIMARY_BACKGROUND_COLOR, 0.75 - percent * 1.5)
            } else {
                ViewColor.HIGHLIGHT_COLOR.interpolate(ViewColor.POINT_RED, percent - 0.75)
            }

            SplineView.Color(
                highlight,
                segment.endProgress
            )
        }
    }

    override val view = SplineView(
        getSourcePointFromPath(reference),
        getTargetPointFromPath(planet.version, reference),
        getControlPointsFromPath(planet.version, reference),
        PlottingConstraints.LINE_WIDTH,
        getSenderGrouping(planet, reference).find { !it.changes }?.viewColor ?: ViewColor.LINE_COLOR,
        generateHighlightColors(planet, reference),
        reference.hidden
    )

    private val isWeightVisibleProperty = property(reference.weight > 0.0)
    private val isBlockedProperty = property(reference.weight < 0.0 || reference.exposure.any {
        it.changes != null && it.changes.weight < 0
    })
    private val isArrowVisibleProperty = property(reference.arrow)

    val isOneWayPath
        get() = reference.source == reference.target && reference.sourceDirection == reference.targetDirection

    private fun getOrthogonal(t: Double, swap: Boolean, distance: Double = 0.1): Pair<Vector, Vector> {
        val position = view.eval(t)
        val gradient = view.evalGradient(t)

        val vec = gradient.orthogonal()

        var source = position + vec * distance
        var target = position - vec * distance

        if (source.left + source.top > target.left + target.top && swap) {
            val h = target
            target = source
            source = h
        }

        return source to target
    }

    private val weightView = TextView(
        getOrthogonal(0.5, true).first,
        12.0,
        reference.weight.toString(),
        ViewColor.LINE_COLOR,
        ICanvas.FontAlignment.CENTER,
        ICanvas.FontWeight.NORMAL
    ) { newValue ->
        val callback = editCallback ?: return@TextView false

        val number = if (newValue.isEmpty()) 1 else newValue.toLongOrNull() ?: return@TextView false

        callback.setPathWeight(reference, number)

        true
    }.also {
        it.animationTime = 0.0
    }

    private val senderGroupView = SenderCharView(
        getOrthogonal(0.5, true, 0.15).second,
        view.evalGradient(0.5),
        getSenderGrouping(planet, reference)
    )
    private val blockedView = BlockedView(
        view.eval(if (isOneWayPath && planet.version >= PlanetVersion.V2020_SPRING) 1.0 else 0.5),
        getSenderGrouping(planet, reference).find { it.changes }?.viewColor ?: ViewColor.POINT_RED,
        !reference.blocked
    ).also {
        it.animationTime = 0.0
    }

    private fun getArrowPosition(): Pair<Vector, Vector> {
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

    override fun onUpdate(obj: PlanetPath, planet: Planet) {
        super.onUpdate(obj, planet)

        val grouping = getSenderGrouping(planet, reference)
        view.setControlPoints(getControlPointsFromPath(planet.version, reference), 0.0)
        view.setSource(getSourcePointFromPath(reference), 0.0)
        view.setTarget(getTargetPointFromPath(planet.version, reference), 0.0)
        view.setColor(getSenderGrouping(planet, reference).find { !it.changes }?.viewColor ?: ViewColor.LINE_COLOR)
        view.setHighlightColor(generateHighlightColors(planet, reference))
        view.setIsDashed(reference.hidden)

        isWeightVisibleProperty.value = reference.weight > 0.0
        isBlockedProperty.value = reference.weight < 0.0 || reference.exposure.any {
            it.changes != null && it.changes.weight < 0
        }
        isArrowVisibleProperty.value = reference.arrow

        weightView.setSource(getOrthogonal(0.5, true).first)
        weightView.text = reference.weight.toString()
        senderGroupView.setCenter(getOrthogonal(0.5, true, 0.15).second)
        senderGroupView.setDirection(view.evalGradient(0.5))
        senderGroupView.setGroupings(grouping)

        blockedView.setCenter(
            view.eval(if (isOneWayPath && planet.version >= PlanetVersion.V2020_SPRING) 1.0 else 0.5)
        )
        blockedView.setColor(getSenderGrouping(planet, reference).find { it.changes }?.viewColor ?: ViewColor.POINT_RED)
        blockedView.setIsPartial(!reference.blocked)

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
                val manager = PathEditManager(
                    this,
                    editCallback
                        ?: throw IllegalStateException("PathEditManager cannot be created if not IEditCallback is present")
                )
                pathEditManager = manager

                manager.onChangePath {
                    view.focus()
                }

                manager
            } else currentManager
        }

    init {
        view += ConditionalView("Path blocked", isBlockedProperty, blockedView)
        view += ConditionalView("Path weight", isWeightVisibleProperty, weightView)
        view += senderGroupView
        view += ConditionalView("Path arrow", isArrowVisibleProperty, arrowView)

        view.focusable = true || editCallback != null
        weightView.focusable = editCallback != null

        if (editCallback != null) {
            view.onFocus {
                view += getOrCreateEditManager.view
            }

            view.onBlur {
                view -= getOrCreateEditManager.view
            }
        }

        view.registerPointerHint(
            {
                val focusedView = view.document?.focusedStack?.lastOrNull() as? SquareView
                if (focusedView != null) {
                    val exposureCoordinate = PlanetPoint(
                        focusedView.center.left.roundToLong(),
                        focusedView.center.top.roundToLong()
                    )
                    "Toggle path exposure (Exposure: ${exposureCoordinate})"
                } else {
                    "Toggle path exposure"
                }
            },
            PointerEvent.Type.DOWN,
            ctrlKey = true
        ) {
            val focusedView = view.document?.focusedStack?.lastOrNull() as? SquareView
            editCallback != null && focusedView != null
        }
        view.registerPointerHint(
            {
                val focusedView = view.document?.focusedStack?.lastOrNull() as? SquareView
                if (focusedView != null) {
                    val exposureCoordinate = PlanetPoint(
                        focusedView.center.left.roundToLong(),
                        focusedView.center.top.roundToLong()
                    )
                    "Toggle path meteorite exposure (Exposure: ${exposureCoordinate})"
                } else {
                    "Toggle path meteorite exposure"
                }
            },
            PointerEvent.Type.DOWN,
            ctrlKey = true,
            shiftKey = true
        ) {
            val focusedView = view.document?.focusedStack?.lastOrNull() as? SquareView
            editCallback != null && focusedView != null
        }
        view.onPointerDown { event ->
            val callback = editCallback ?: return@onPointerDown
            val focusedView = view.document?.focusedStack?.lastOrNull() as? SquareView
            if (event.ctrlKey && focusedView != null) {
                val exposureCoordinate = PlanetPoint(
                    focusedView.center.left.roundToLong(),
                    focusedView.center.top.roundToLong()
                )

                callback.togglePathExposure(this.reference, PlanetPathExposure(
                    exposureCoordinate,
                    if (event.shiftKey) PlanetPathExposureChanges(-1L) else null
                ))

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
                    callback.updatePathSpline(path, null)
                }

                action(if (path.hidden) "Mark path as visible" else "Mark path as hidden") {
                    callback.togglePathHiddenState(path)
                }

                action(if (path.weight < 0) "Unblock path" else "Block path") {
                    callback.setPathWeight(path, if (path.weight < 0) 1 else -1)
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

        view.registerKeyHint(
            "Delete path",
            KeyCode.DELETE
        ) {
            editCallback != null
        }
        view.onKeyPress { event ->
            val callback = editCallback ?: return@onKeyPress

            when (event.keyCode) {
                KeyCode.DELETE -> {
                    callback.deletePath(this.reference)
                }
                else -> return@onKeyPress
            }

            event.stopPropagation()
        }
    }

    private fun getSenderGrouping(planet: Planet, path: PlanetPath): List<SenderGrouping> {
        val senders = path.exposure
            .groupBy { it.changes }
            .mapValues { (_, group) ->
                group.map { it.planetPoint }.toSet()
            }.map { (k, v) -> v to k }.toMap()
        val groupings = planet.senderGroupings.filter { it.sender in senders }
        return groupings.map { SenderGrouping(it.name.first(), senders[it.sender] != null) }
    }

    companion object {
        fun getSourcePointFromPath(path: PlanetPath): Vector {
            return path.source.point
        }

        fun getTargetPointFromPath(version: Long, path: PlanetPath): Vector {
            if (path.isOneWayPath && version >= PlanetVersion.V2020_SPRING) {
                return getControlPointsFromPath(version, path).last()
            }

            return path.target.point
        }

        fun getControlPointsFromPath(
            version: Long,
            path: PlanetPath,
        ): List<Vector> {
            return getControlPointsFromPath(
                version,
                path.source.point,
                path.sourceDirection,
                path.target.point,
                path.targetDirection,
                path.spline
            )
        }

        private fun isPointInDirectLine(start: Vector, direction: PlanetDirection, target: Vector): Boolean =
            when (direction) {
                PlanetDirection.North -> start.y <= target.y && start.x == target.x
                PlanetDirection.East -> start.x <= target.x && start.y == target.y
                PlanetDirection.South -> start.y >= target.y && start.x == target.x
                PlanetDirection.West -> start.x >= target.x && start.y == target.y
            }

        fun getControlPointsFromPath(
            version: Long,
            source: Vector,
            sourceDirection: PlanetDirection,
            target: Vector,
            targetDirection: PlanetDirection,
            spline: PlanetSpline? = null,
        ): List<Vector> {
            if (source == target && sourceDirection == targetDirection && version >= PlanetVersion.V2020_SPRING) {
                val linePoints = spline?.controlPoints?.map { it.point }
                    ?: listOf(source.shift(sourceDirection, PlottingConstraints.CURVE_SECOND_POINT))

                return listOfNotNull(
                    source.shift(sourceDirection, PlottingConstraints.CURVE_FIRST_POINT),
                    if (linePoints.isNotEmpty() && isPointInDirectLine(
                            source,
                            sourceDirection,
                            linePoints.first()
                        )
                    ) null else source.shift(sourceDirection, PlottingConstraints.CURVE_SECOND_POINT),
                    *linePoints.toTypedArray()
                )
            }

            val linePoints = spline?.controlPoints?.map { it.point }
                ?: PathGenerator.generateControlPoints(
                    version,
                    source,
                    sourceDirection,
                    target,
                    targetDirection
                )

            return listOfNotNull(
                source.shift(sourceDirection, PlottingConstraints.CURVE_FIRST_POINT),
                if (linePoints.isNotEmpty() && isPointInDirectLine(
                        source,
                        sourceDirection,
                        linePoints.first()
                    )
                ) null else source.shift(sourceDirection, PlottingConstraints.CURVE_SECOND_POINT),
                *linePoints.toTypedArray(),
                if (linePoints.isNotEmpty() && isPointInDirectLine(
                        target,
                        targetDirection,
                        linePoints.last()
                    )
                ) null else target.shift(targetDirection, PlottingConstraints.CURVE_SECOND_POINT),
                target.shift(targetDirection, PlottingConstraints.CURVE_FIRST_POINT)
            )
        }

        inline fun multiEval(
            count: Int,
            controlPoints: List<Vector>,
            startPoint: Vector,
            endPoint: Vector?,
            eval: (Double) -> Vector,
        ): List<Vector> {
            val realCount = max(16, power2(log2(count - 1) + 1))

            val points = arrayOfNulls<Vector>(realCount + 1)

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

        fun evalLength(planetVersion: Long, path: PlanetPath): Double {
            val controlPoints = getControlPointsFromPath(planetVersion, path)
            val lengthEstimate = (listOf(path.source.point) + controlPoints + path.target.point).windowed(2, 1)
                .sumOf { (p0, p1) -> p0.distanceTo(p1) }
            val evalCount = (lengthEstimate * 10).roundToInt()

            val source = getSourcePointFromPath(path)
            val target = getTargetPointFromPath(planetVersion, path)

            val eval = listOf(source) + CurveEval.evalSpline(
                evalCount,
                controlPoints,
                source,
                target,
                BSpline
            ) + target

            val factor = if (path.isOneWayPath && planetVersion < PlanetVersion.V2020_SPRING) 0.5 else 1.0

            return eval.windowed(2, 1)
                .sumOf { (p0, p1) -> p0.distanceTo(p1) } * factor
        }
    }
}

val SenderGrouping.viewColor
    get() = ViewColor.c(color)
