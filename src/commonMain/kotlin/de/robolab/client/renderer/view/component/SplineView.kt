package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.drawable.utils.BSpline
import de.robolab.client.renderer.drawable.utils.Curve
import de.robolab.client.renderer.drawable.utils.CurveEval
import de.robolab.client.renderer.drawable.utils.c
import de.robolab.client.renderer.transition.IInterpolatable
import de.robolab.client.renderer.view.base.BaseView
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.common.utils.Point
import de.robolab.common.utils.Rectangle
import de.robolab.common.utils.unionNullable
import de.westermann.kobserve.property.property

class SplineView(
    source: Point,
    target: Point,
    controlPoints: List<Point>,
    width: Double,
    color: ViewColor,
    highlightColor: List<Color>,
    isDashed: Boolean
) : BaseView() {

    class Color(
        val color: ViewColor,
        val t: Double = 1.0
    ) : IInterpolatable<Color> {
        override fun interpolate(toValue: Color, progress: Double): Color {
            return Color(
                color.interpolate(toValue.color, progress),
                t + (toValue.t - t) * progress,
            )
        }

        override fun interpolateToNull(progress: Double): Color {
            return Color(
                color.interpolateToNull(progress),
                t,
            )
        }
    }

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

    private val highlightColorTransition = transition(highlightColor)
    val highlightColor by highlightColorTransition
    fun setHighlightColor(highlightColor: List<Color>, duration: Double = animationTime, offset: Double = 0.0) {
        highlightColorTransition.animate(highlightColor, duration, offset)
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


    private val pointHelperCache = mutableMapOf<Int, List<CurveEval>>()
    private val distance by property(sourceTransition, targetTransition, controlPointsTransition) {
        pointHelperCache.clear()
        (listOf(this.source) + this.controlPoints + this.target).windowed(2, 1)
            .sumByDouble { (p0, p1) -> p0.distanceTo(p1) }
    }

    private val curve: Curve = BSpline

    fun eval(t: Double): Point {
        return curve.eval(t, controlPoints)
    }

    fun evalGradient(t: Double): Point {
        return curve.evalGradient(t, controlPoints)
    }

    private var stepCount: Int = 1
    private fun calcLineSegments(breaks: List<Double> = emptyList()): List<List<Point>> {
        if (progress == 0.0) return emptyList()

        val pointHelpers = pointHelperCache.getOrPut(stepCount) {
            CurveEval.evalSplineAttributed(stepCount, controlPoints, source, target, curve)
        }

        val ranges = mutableListOf<Pair<Double, Double>>()
        if (progress == 1.0 || progress == -1.0) {
            var rangeStart = 0.0
            for (b in breaks) {
                ranges += rangeStart to b
                rangeStart = b
            }
            ranges += rangeStart to 1.0
        } else if (progress > 0.0) {
            var rangeStart = 0.0
            for (b in breaks) {
                if (b >= progress) break
                ranges += rangeStart to b
                rangeStart = b
            }
            ranges += rangeStart to progress
        } else {
            var rangeStart = 1.0
            for (b in breaks.reversed()) {
                if (b <= -progress) break
                ranges += b to rangeStart
                rangeStart = b
            }
            ranges += -progress to rangeStart
            ranges.reverse()
        }

        val result = ranges.map { mutableListOf<Point>() }
        var rangeIndex = 0
        var range = ranges[rangeIndex]
        var current = result[rangeIndex]

        var pi = 0
        for (p in pointHelpers) {
            if (p.curveProgress < range.first) continue
            if (p.curveProgress < range.second) {
                current.add(p.point)
            } else {
                val last = pointHelpers[pi - 1]
                val interpolateProgress = (range.second - last.curveProgress) / (p.curveProgress - last.curveProgress)
                val point = last.point.interpolate(p.point, interpolateProgress)

                current.add(point)

                rangeIndex += 1
                if (rangeIndex >= ranges.size) break
                range = ranges[rangeIndex]
                current = result[rangeIndex]

                current.add(point)
            }
            pi += 1
        }

        return result
    }

    override fun onDraw(context: DrawContext) {
        stepCount = ((distance * context.transformation.scaledGridWidth) / 5).toInt()

        val breaks = if (isHovered || isFocused) {
            highlightColor.dropLast(1).map { it.t }
        } else emptyList()
        val segments = calcLineSegments(breaks) zip highlightColor

        if (segments.isEmpty()) return

        for ((points, colors) in segments) {
            if (points.isEmpty()) continue

            if (isHovered && !isFocused) {
                val c = context.c(colors.color)
                context.strokeLine(points, c, width * 3)
            }
            if (isFocused) {
                val c = context.c(colors.color)
                context.strokeLine(points, c, width * 5)
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
        }

        super.onDraw(context)
    }

    override fun calculateBoundingBox(): Rectangle? {
        val parentBox = super.calculateBoundingBox()
        return Rectangle.fromEdges(listOf(source) + controlPoints + target).expand(width / 2) unionNullable parentBox
    }

    override fun checkPoint(planetPoint: Point, canvasPoint: Point, epsilon: Double): Boolean {
        val segments = calcLineSegments()

        if (segments.isEmpty()) return false

        return segments.any { segment ->
            segment.any {
                it.distanceTo(planetPoint) - epsilon < width / 2
            }
        }
    }

    override fun debugStringParameter(): List<Any?> {
        return listOf(source, target)
    }

    override fun onCreate() {
        setProgress(1.0)
    }

    override fun onDestroy(onFinish: () -> Unit) {
        setProgress(0.0)

        animatableManager.onFinish(onFinish)
    }
}
