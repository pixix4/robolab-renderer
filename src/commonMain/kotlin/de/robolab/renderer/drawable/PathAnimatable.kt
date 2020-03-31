package de.robolab.renderer.drawable

import de.robolab.model.Direction
import de.robolab.model.Path
import de.robolab.model.Planet
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.drawable.base.Animatable
import de.robolab.renderer.drawable.utils.*
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.utils.DrawContext
import kotlin.math.max


class PathAnimatable(
        reference: Path,
        private val planetDrawable: PlanetDrawable,
        planet: Planet
) : Animatable<Path>(reference) {

    private val startPoint: Point = reference.source.let { Point(it.x.toDouble(), it.y.toDouble()) }
    private val startDirection: Direction = reference.sourceDirection
    private val endPoint: Point = reference.target.let { Point(it.x.toDouble(), it.y.toDouble()) }
    private var weight = reference.weight
    private val isOneWayPath = reference.source == reference.target && reference.sourceDirection == reference.targetDirection
    private val evalEndPoint = if (isOneWayPath) null else endPoint

    private var state = State.NONE

    private var controlPoints = getControlPointsFromPath(reference)
    private var area = Rectangle.fromEdges(startPoint, endPoint, *controlPoints.toTypedArray())
    private var distance = controlPoints.windowed(2, 1).sumByDouble { (p1, p2) ->
        p1.distance(p2)
    }

    private val curve: Curve = BSpline

    private fun eval(t: Double): Point {
        return curve.eval(t, controlPoints)
    }

    private fun multiEval(count: Int): List<Point> {
        return Companion.multiEval(count, controlPoints, startPoint, evalEndPoint, this::eval)
    }

    data class PointLengthHelper(
            val point: Point,
            var length: Double = 0.0
    )

    private val pointHelperCache = mutableMapOf<Int, List<PointLengthHelper>>()
    private fun getCachedPointHelpers(steps: Int): List<PointLengthHelper> {
        return pointHelperCache.getOrPut(steps) {
            val pointHelpers = multiEval(steps).map { PointLengthHelper(it) }

            for ((p1, p2) in pointHelpers.windowed(2, 1)) {
                p2.length = p1.length + p2.point.distance(p1.point)
            }

            pointHelpers
        }
    }

    private fun interpolateLineEnd(pointHelpers: List<PointLengthHelper>, endIndex: Int, targetLength: Double): List<Point> {
        val p1 = pointHelpers[endIndex - 1]
        val p2 = pointHelpers[endIndex]

        val endPoint = p1.point.interpolate(p2.point, (targetLength - p1.length) / (p2.length - p1.length))

        return pointHelpers.take(endIndex).map { it.point } + endPoint
    }

    private fun interpolate(context: DrawContext) {
        val steps = ((distance * context.transformation.scaledGridWidth) / 10).toInt()

        val isHover = reference in this.planetDrawable.hoveredPaths || reference == this.planetDrawable.selectedPath

        val oc = oldColor ?: context.theme.lineColor
        val nc = newColor ?: context.theme.lineColor
        val color = oc.interpolate(nc, colorTransition.value)

        fun draw(points: List<Point>, color: Color, weight: Double) {
            if (hiddenTransition.value > 0.0) {
                val spacingLength = PlottingConstraints.DASH_SPACING * hiddenTransition.value
                val dashes = listOf(
                        PlottingConstraints.DASH_SEGMENT_LENGTH - spacingLength,
                        spacingLength
                )
                context.dashLine(points, color, weight, dashes, dashes.first() / 2)
            } else {
                context.strokeLine(points, color, weight)
            }
        }

        when (state) {
            State.REMOVE -> {
                val points = getCachedPointHelpers(steps).map { it.point }
                if (isHover) {
                    context.strokeLine(points, context.theme.highlightColor, PlottingConstraints.LINE_HOVER_WIDTH)
                }
                draw(points, color.a(transition.value), PlottingConstraints.LINE_WIDTH)
            }
            State.DRAW -> {
                val pointHelpers = getCachedPointHelpers(steps)

                val length = pointHelpers.last().length
                val targetLength = length * transition.value

                val endIndex = pointHelpers.indexOfFirst { it.length >= targetLength }

                if (endIndex <= 0) {
                    return
                }

                val points = interpolateLineEnd(pointHelpers, endIndex, targetLength)
                if (isHover) {
                    context.strokeLine(points, context.theme.highlightColor, PlottingConstraints.LINE_HOVER_WIDTH)
                }
                draw(points, color, PlottingConstraints.LINE_WIDTH)
            }
            State.NONE -> {
                val points = getCachedPointHelpers(steps).map { it.point }
                if (isHover) {
                    context.strokeLine(points, context.theme.highlightColor, PlottingConstraints.LINE_HOVER_WIDTH)
                }
                draw(points, color, PlottingConstraints.LINE_WIDTH)
            }
        }
    }

    private fun calcCenterAt(before: Double, center: Double, after: Double? = null, swap: Boolean = true): Pair<Point, Point> {

        val beforeCenter = eval(before)
        val centerPoint = eval(center)

        val diffPoint = if (after == null) {
            centerPoint
        } else {
            eval(after)
        }

        val centerDirection = (diffPoint - beforeCenter).let {
            val h = Point(it.y, -it.x).normalize()
            if (h == Point.ZERO) {
                val h2 = Point(centerPoint.y - beforeCenter.y, beforeCenter.x - centerPoint.x).normalize()
                if (h2 == Point.ZERO) {
                    when (startDirection) {
                        Direction.NORTH, Direction.SOUTH -> Point(1.0, 0.0)
                        else -> Point(0.0, 1.0)
                    }
                } else h2
            } else h
        } * 0.1
        return ((centerPoint - centerDirection) to (centerPoint + centerDirection)).let {
            if (it.first < it.second && swap) it else it.second to it.first
        }
    }

    private fun getAlpha(rangeStart: Double, rangeEnd: Double): Double {
        return when {
            state == State.REMOVE -> transition.value
            transition.value <= rangeStart -> 0.0
            transition.value >= rangeEnd -> 1.0
            else -> (transition.value - rangeStart) / (rangeEnd - rangeStart)
        }
    }

    override fun onDraw(context: DrawContext) {
        if (!context.area.intersects(area)) return

        interpolate(context)

        val weight = weight
        if (weight == null) {
            val (downLeftCenter, topRightCenter) = if (isOneWayPath) {
                calcCenterAt(0.995, 1.0, swap = false)
            } else {
                calcCenterAt(0.49, 0.5, 0.51, swap = false)
            }

            val alpha = if (isOneWayPath) getAlpha(0.8, 1.0) else getAlpha(0.4, 0.6)

            val top = topRightCenter + (downLeftCenter - topRightCenter).let { Point(it.top, -it.left) }.normalize() * PlottingConstraints.ARROW_LENGTH
            drawArrow(context, topRightCenter, top, context.theme.lineColor.a(alpha))
            return
        }

        if (weight >= 0) {
            val (downLeftCenter, _) = calcCenterAt(0.49, 0.5, 0.51)
            val alpha = getAlpha(0.4, 0.6)
            context.fillText(weight.toString(), downLeftCenter, context.theme.gridTextColor.a(alpha), 12.0, alignment = ICanvas.FontAlignment.CENTER)
        } else {
            val (downLeftCenter, topRightCenter) = if (isOneWayPath) {
                calcCenterAt(0.995, 1.0)
            } else {
                calcCenterAt(0.49, 0.5, 0.51)
            }

            val alpha = if (isOneWayPath) getAlpha(0.8, 1.0) else getAlpha(0.4, 0.6)

            context.strokeLine(
                    listOf(
                            downLeftCenter,
                            topRightCenter
                    ),
                    context.theme.redColor.a(alpha),
                    PlottingConstraints.LINE_WIDTH * 1.5
            )
        }
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        val steps = ((distance * context.transformation.scaledGridWidth) / 10).toInt()

        val points = when (state) {
            State.REMOVE, State.NONE -> {
                getCachedPointHelpers(steps).map { it.point }
            }
            State.DRAW -> {
                val pointHelpers = getCachedPointHelpers(steps)

                val length = pointHelpers.last().length
                val targetLength = length * transition.value

                val endIndex = pointHelpers.indexOfFirst { it.length > targetLength }

                if (endIndex == 0) {
                    emptyList()
                } else {
                    interpolateLineEnd(pointHelpers, endIndex, targetLength)
                }
            }
        }

        if (points.any { it.distance(position) < 0.1 }) {
            return listOf(reference)
        }

        return emptyList()
    }

    companion object {
        fun log2(a: Int): Int {
            var x = a
            var pow = 0
            if (x >= 1 shl 16) {
                x = x shr 16
                pow += 16
            }
            if (x >= 1 shl 8) {
                x = x shr 8
                pow += 8
            }
            if (x >= 1 shl 4) {
                x = x shr 4
                pow += 4
            }
            if (x >= 1 shl 2) {
                x = x shr 2
                pow += 2
            }
            if (x >= 1 shl 1) {
                //x = x shr 1
                pow += 1
            }
            return pow
        }

        fun power2(exp: Int): Int {
            var x = 2
            var y = exp
            var result = 1
            while (y > 0) {
                if (y and 1 == 0) {
                    x *= x
                    y = y ushr 1
                } else {
                    result *= x
                    y--
                }
            }
            return result
        }


        fun getControlPointsFromPath(path: Path): List<Point> {
            val startPoint = Point(path.source.x, path.source.y)
            val startDirection = path.sourceDirection
            val endPoint = Point(path.target.x, path.target.y)
            val endDirection = path.targetDirection

            return getControlPointsFromPath(startPoint, startDirection, endPoint, endDirection, path.controlPoints.map { Point(it.x, it.y) })
        }

        fun getControlPointsFromPath(startPoint: Point, startDirection: Direction, endPoint: Point, endDirection: Direction, controlPoints: List<Point> = emptyList()): List<Point> {
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

        fun drawArrow(context: DrawContext, bottom: Point, top: Point, color: Color) {
            context.strokeLine(
                    listOf(bottom, top.interpolate(bottom, 0.3)),
                    color,
                    PlottingConstraints.LINE_WIDTH * 0.65
            )

            val arrowMiddle = top.interpolate(bottom, 0.4)
            val vector = (arrowMiddle - top) * 0.7
            val left = arrowMiddle + Point(vector.top, -vector.left)
            val right = arrowMiddle + Point(-vector.top, vector.left)
            context.fillPolygon(
                    listOf(top, left, right),
                    color
            )
        }
    }

    private var oldColor: Color? = getColor(planet, reference)
    private var newColor: Color? = oldColor

    private val colorTransition = DoubleTransition(0.0)
    private val transition = DoubleTransition(0.0)
    private val hiddenTransition = DoubleTransition(if (reference.hidden) 1.0 else 0.0)

    override val animators = listOf(transition, colorTransition, hiddenTransition)

    override fun startExitAnimation(onFinish: () -> Unit) {
        state = State.REMOVE
        transition.animate(0.0, planetDrawable.animationTime / 3)
        transition.onFinish.clearListeners()
        transition.onFinish {
            state = State.NONE
            onFinish()
        }
    }

    override fun startEnterAnimation(onFinish: () -> Unit) {
        state = State.DRAW
        transition.animate(1.0, planetDrawable.animationTime)
        transition.onFinish.clearListeners()
        transition.onFinish {
            state = State.NONE
            onFinish()
        }
    }

    override fun startUpdateAnimation(obj: Path, planet: Planet) {
        weight = obj.weight
        reference = obj
        controlPoints = getControlPointsFromPath(obj)

        oldColor = newColor
        newColor = getColor(planet, obj)
        colorTransition.resetValue(0.0)
        colorTransition.animate(1.0, planetDrawable.animationTime)
        hiddenTransition.animate(if (reference.hidden) 1.0 else 0.0, planetDrawable.animationTime)

        area = Rectangle.fromEdges(startPoint, endPoint, *controlPoints.toTypedArray())
        distance = controlPoints.windowed(2, 1).sumByDouble { (p1, p2) ->
            p1.distance(p2)
        }

        pointHelperCache.clear()
    }

    private fun getColor(planet: Planet, path: Path): Color? {
        if (path.exposure.isEmpty()) {
            return null
        }
        return Utils.getColorByIndex(Utils.getSenderGrouping(planet).getValue(path.exposure))
    }

    enum class State {
        DRAW, REMOVE, NONE
    }
}
