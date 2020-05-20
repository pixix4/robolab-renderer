package de.robolab.renderer.document

import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.data.unionNullable
import de.robolab.renderer.document.base.BaseView
import de.robolab.renderer.drawable.utils.*
import de.robolab.renderer.utils.DrawContext
import de.westermann.kobserve.event.once
import de.westermann.kobserve.property.property
import kotlin.math.max

class SplineView(
        source: Point,
        target: Point,
        controlPoints: List<Point>,
        width: Double,
        color: ViewColor,
        isDashed: Boolean
) : BaseView() {


    private val sourceTransition = transition(source)
    val source by sourceTransition
    fun setSource(source: Point, duration: Double = animationTime, offset: Double = 0.0) {
        sourceTransition.animate(source, duration, offset)
    }

    private val targetTransition = transition(target)
    val target by targetTransition
    fun setTarget(target: Point, duration: Double = animationTime, offset: Double = 0.0) {
        targetTransition.animate(target, duration, offset)
    }

    private val controlPointsTransition = transition(controlPoints)
    val controlPoints by controlPointsTransition
    fun setControlPoints(controlPoints: List<Point>, duration: Double = animationTime, offset: Double = 0.0) {
        controlPointsTransition.animate(controlPoints, duration, offset)
    }

    private val widthTransition = transition(width)
    val width by widthTransition
    fun setWidth(width: Double, duration: Double = animationTime, offset: Double = 0.0) {
        widthTransition.animate(width, duration, offset)
    }

    private val colorTransition = transition(color)
    val color by colorTransition
    fun setColor(color: ViewColor, duration: Double = animationTime, offset: Double = 0.0) {
        colorTransition.animate(color, duration, offset)
    }

    private val progressTransition = transition(0.0)
    val progress by progressTransition
    fun setProgress(progress: Double, duration: Double = animationTime, offset: Double = 0.0) {
        progressTransition.animate(progress, duration, offset)
    }

    private val dashedTransition = transition(if (isDashed) 1.0 else 0.0)
    val dashed by dashedTransition
    fun setIsDashed(isDashed: Boolean, duration: Double = animationTime, offset: Double = 0.0) {
        dashedTransition.animate(if (isDashed) 1.0 else 0.0, duration, offset)
    }


    private val pointHelperCache = mutableMapOf<Int, List<PointLengthHelper>>()
    private val distance by property(sourceTransition, targetTransition, controlPointsTransition) {
        pointHelperCache.clear()
        (listOf(source) + controlPoints + target).windowed(2, 1).sumByDouble { (p0, p1) -> p0.distanceTo(p1) }
    }

    private val curve: Curve = BSpline

    fun eval(t: Double): Point {
        return curve.eval(t, controlPoints)
    }

    fun evalGradient(t: Double): Point {
        return curve.evalGradient(t, controlPoints)
    }

    private var stepCount: Int = 1
    private fun calcLinePoints(): List<Point> {
        if (progress == 0.0) return emptyList()

        val pointHelpers = pointHelperCache.getOrPut(stepCount) {
            val pointHelpers = evalSpline(stepCount, controlPoints, source, target, curve).map {
                PointLengthHelper(it)
            }

            for ((p1, p2) in pointHelpers.windowed(2, 1)) {
                p2.length = p1.length + p2.point.distanceTo(p1.point)
            }

            pointHelpers
        }


        return if (progress == 1.0 || progress == -1.0) {
            // Draw full spline

            pointHelpers.map { it.point }
        } else if (progress > 0.0) {
            // Draw partial spline from source

            val curveLength = pointHelpers.last().length
            val progressLength = curveLength * progress

            val endIndex = pointHelpers.indexOfFirst { it.length >= progressLength }

            if (endIndex < 0) {
                return emptyList()
            }

            val index = if (endIndex > 1) endIndex else 1
            val p1 = pointHelpers[index - 1]
            val p2 = pointHelpers[index]

            val endPoint = p1.point.interpolate(p2.point, (progressLength - p1.length) / (p2.length - p1.length))

            pointHelpers.take(index).map { it.point } + endPoint
        } else {
            // Draw partial spline from target

            val curveLength = pointHelpers.last().length
            val progressLength = curveLength * (1 + progress)

            val endIndex = pointHelpers.indexOfLast { it.length <= progressLength }

            if (endIndex < 0) {
                pointHelpers.map { it.point }
            } else {
                val index = if (endIndex > 1) endIndex else 1
                val p2 = pointHelpers[index - 1]
                val p1 = pointHelpers[index]

                val endPoint = p1.point.interpolate(p2.point, (progressLength - p1.length) / (p2.length - p1.length))

                pointHelpers.take(index).map { it.point } + endPoint
            }
        }
    }

    override fun onDraw(context: DrawContext) {
        stepCount = ((distance * context.transformation.scaledGridWidth) / 5).toInt()
        val points = calcLinePoints()

        if (points.isEmpty()) return
        
        if (isHovered && !isFocused) {
            context.strokeLine(points, context.theme.plotter.highlightColor, width * 3)
        }
        if (isFocused) {
            context.strokeLine(points, context.theme.plotter.highlightColor, width * 5)
        }

        if (dashed > 0.0) {
            val spacingLength = PlottingConstraints.DASH_SPACING * dashed
            val dashes = listOf(
                    PlottingConstraints.DASH_SEGMENT_LENGTH - spacingLength,
                    spacingLength
            )
            context.dashLine(points, context.c(color), width, dashes, dashes.first() / 2)
        } else {
            context.strokeLine(points, context.c(color), width)
        }

        super.onDraw(context)
    }

    override fun calculateBoundingBox(): Rectangle? {
        val parentBox = super.calculateBoundingBox()
        return Rectangle.fromEdges(listOf(source) + controlPoints + target).expand(width / 2) unionNullable parentBox
    }

    override fun checkPoint(planetPoint: Point, canvasPoint: Point, epsilon: Double): Boolean {
        val points = calcLinePoints()

        if (points.isEmpty()) return false

        return points.any { it.distanceTo(planetPoint) - epsilon < width / 2 }
    }

    data class PointLengthHelper(
            val point: Point,
            var length: Double = 0.0
    )

    override fun onCreate() {
        setProgress(1.0)
    }

    override fun onDestroy(onFinish: () -> Unit) {
        setProgress(0.0)

        animatableManager.onFinish(onFinish)
    }

    companion object {
        fun evalSpline(count: Int, controlPoints: List<Point>, source: Point, target: Point, curve: Curve): List<Point> {
            val realCount = max(16, power2(log2(count - 1) + 1))

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

            points[index] = (controlPoints.last())

            val startPointEdge = source + (controlPoints.first() - source).normalize() * PlottingConstraints.POINT_SIZE / 2
            val endPointEdge = target + (controlPoints.last() - target).normalize() * PlottingConstraints.POINT_SIZE / 2
            return listOf(startPointEdge) + points.take(index + 1).requireNoNulls() + endPointEdge
        }
    }
}
