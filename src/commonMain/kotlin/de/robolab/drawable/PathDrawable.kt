package de.robolab.drawable

import de.robolab.drawable.curve.BSpline
import de.robolab.drawable.curve.Curve
import de.robolab.drawable.utils.PathGenerator
import de.robolab.drawable.utils.shift
import de.robolab.model.Direction
import de.robolab.model.Path
import de.robolab.model.Planet
import de.robolab.renderer.DrawContext
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.animation.DoubleTransition
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle


class PathDrawable(
        path: Path,
        private val plotter: PlanetDrawable
) : Animatable<Path>(path) {

    val startPoint: Point = path.source.let { Point(it.first.toDouble(), it.second.toDouble()) }
    val startDirection: Direction = path.sourceDirection
    val endPoint: Point = path.target.let { Point(it.first.toDouble(), it.second.toDouble()) }
    val endDirection: Direction = path.targetDirection
    private var weight: Int = path.weight

    private var state = State.NONE

    private val linePoints: List<Point> = null
            ?: PathGenerator.generateControlPoints(startPoint, startDirection, endPoint, endDirection)

    private val controlPoints = linePointsToControlPoints(linePoints, startPoint, startDirection, endPoint, endDirection)

    val area by lazy {
        Rectangle.fromEdges(startPoint, endPoint, *this.controlPoints.toTypedArray())
    }

    private val distance by lazy {
        (listOf(startPoint) + this.controlPoints + endPoint).windowed(2, 1).sumByDouble { (p1, p2) ->
            p1.distance(p2)
        }
    }

    private val curve: Curve = BSpline

    private val cache: MutableMap<Double, Point> = mutableMapOf()
    private fun eval(t: Double): Point {
        return cache.getOrPut(t) {
            curve.eval(t, controlPoints)
        }
    }

    private fun multiEval(count: Int): List<Point> {
        val realCount = power2(log2(count - 1) + 1)

        val points = arrayOfNulls<Point>(realCount + 1)

        val step = 1.0 / realCount
        var t = 2 * step

        points[0] = controlPoints.first()

        var index = 1
        while (t < 1.0) {
            points[index] = (cache.getOrPut(t) {
                curve.eval(t - step, controlPoints)
            })
            t += step
            index += 1
        }

        points[index] = (controlPoints.last())

        val startPoint = startPoint + (controlPoints.first() - startPoint).normalize() * PlottingConstraints.POINT_SIZE / 2
        val endPoint = endPoint + (controlPoints.last() - endPoint).normalize() * PlottingConstraints.POINT_SIZE / 2

        return listOf(startPoint) + points.take(index + 1).requireNoNulls() + endPoint
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

    private fun interpolate(context: DrawContext) {
        val steps = ((distance * context.transformation.scaledGridWidth) / 10).toInt()

        when (state) {
            State.REMOVE -> {
                val points = getCachedPointHelpers(steps).map { it.point }
                if (hoverTransition.value > 0) {
                    context.strokeLine(points, context.theme.highlightColor, PlottingConstraints.LINE_HOVER_WIDTH * hoverTransition.value)
                }
                context.strokeLine(points, context.theme.lineColor.a(transition.value), PlottingConstraints.LINE_WIDTH)
            }
            State.DRAW -> {
                val pointHelpers = getCachedPointHelpers(steps)

                val length = pointHelpers.last().length
                val targetLength = length * transition.value

                val endIndex = pointHelpers.indexOfFirst { it.length > targetLength }

                if (endIndex == 0) return

                val p1 = pointHelpers[endIndex - 1]
                val p2 = pointHelpers[endIndex]

                val endPoint = p1.point.interpolate(p2.point, (targetLength - p1.length) / (p2.length - p1.length))

                val points = pointHelpers.take(endIndex).map { it.point } + endPoint
                if (hoverTransition.value > 0) {
                    context.strokeLine(points, context.theme.highlightColor, PlottingConstraints.LINE_HOVER_WIDTH * hoverTransition.value)
                }
                context.strokeLine(points, context.theme.lineColor, PlottingConstraints.LINE_WIDTH)
            }
            State.NONE -> {
                val points = getCachedPointHelpers(steps).map { it.point }
                if (hoverTransition.value > 0) {
                    context.strokeLine(points, context.theme.highlightColor, PlottingConstraints.LINE_HOVER_WIDTH * hoverTransition.value)
                }
                context.strokeLine(points, context.theme.lineColor, PlottingConstraints.LINE_WIDTH)
            }
        }
    }

    override fun onDraw(context: DrawContext) {
        if (!context.area.intersects(area)) return

        interpolate(context)

        val alpha = when {
            state == State.REMOVE -> transition.value
            transition.value <= 0.4 -> return
            transition.value >= 0.6 -> 1.0
            else -> (transition.value - 0.4) * 5
        }

        val beforeCenter = eval(0.49)
        val center = eval(0.5)
        val afterCenter = eval(0.51)

        val centerDirection = (beforeCenter - afterCenter).let {
            val h = Point(it.y, -it.x).normalize()
            if (h == Point.ZERO) {
                val h2 = Point(center.y - afterCenter.y, afterCenter.x - center.x).normalize()
                if (h2 == Point.ZERO) {
                    when (startDirection) {
                        Direction.NORTH, Direction.SOUTH -> Point(1.0, 0.0)
                        else -> Point(0.0, 1.0)
                    }
                } else h2
            } else h
        } * 0.1
        val (downLeftCenter, topRightCenter) = ((center - centerDirection) to (center + centerDirection)).let {
            if (it.first < it.second) it else it.second to it.first
        }

        if (weight >= 0) {
            context.fillText(weight.toString(), downLeftCenter, context.theme.gridTextColor.a(alpha), 12.0)
        } else {
            context.strokeLine(
                    listOf(
                            downLeftCenter,
                            topRightCenter
                    ),
                    context.theme.redColor.a(alpha),
                    4.0
            )
        }
    }

    override fun getObjectAtPosition(context: DrawContext, position: Point): Any? {
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
                    val p1 = pointHelpers[endIndex - 1]
                    val p2 = pointHelpers[endIndex]

                    val endPoint = p1.point.interpolate(p2.point, (targetLength - p1.length) / (p2.length - p1.length))

                    val points = pointHelpers.take(endIndex).map { it.point } + endPoint
                    points
                }
            }
        }

        if (points.any { it.distance(position) < 0.1 }) {
            return reference
        }

        return null
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

        fun linePointsToControlPoints(
                linePoints: List<Point>,
                startPoint: Point,
                startDirection: Direction,
                endPoint: Point,
                endDirection: Direction
        ) = listOfNotNull(
                startPoint.shift(startDirection, PlottingConstraints.CURVE_FIRST_POINT),
                if (linePoints.isNotEmpty() && PathGenerator.isPointInDirectLine(startPoint, startDirection, linePoints.first())) null else startPoint.shift(startDirection, PlottingConstraints.CURVE_SECOND_POINT),
                *linePoints.toTypedArray(),
                if (linePoints.isNotEmpty() && PathGenerator.isPointInDirectLine(endPoint, endDirection, linePoints.last())) null else endPoint.shift(endDirection, PlottingConstraints.CURVE_SECOND_POINT),
                endPoint.shift(endDirection, PlottingConstraints.CURVE_FIRST_POINT)
        )
    }

    private val transition = DoubleTransition(0.0)
    private val hoverTransition = DoubleTransition(0.0)

    override val animators = listOf(transition, hoverTransition)

    override fun startExitAnimation(onFinish: () -> Unit) {
        state = State.REMOVE
        transition.animate(0.0, plotter.animationTime / 3)
        hoverTransition.animate(0.0, plotter.animationTime / 3)
        transition.onFinish.clearListeners()
        transition.onFinish {
            state = State.NONE
            onFinish()
        }
    }

    override fun startEnterAnimation(onFinish: () -> Unit) {
        state = State.DRAW
        transition.animate(1.0, plotter.animationTime)
        transition.onFinish.clearListeners()
        transition.onFinish {
            state = State.NONE
            onFinish()
        }
    }

    override fun startUpdateAnimation(obj: Path, planet: Planet) {
        weight = obj.weight

        val hoverValue = if (obj in plotter.hoveredElements) 1.0 else 0.0
        hoverTransition.animate(hoverValue, plotter.animationTime / 3)
    }

    enum class State {
        DRAW, REMOVE, NONE
    }
}
