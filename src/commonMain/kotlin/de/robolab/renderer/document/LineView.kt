package de.robolab.renderer.document

import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.data.unionNullable
import de.robolab.renderer.document.base.BaseView
import de.robolab.renderer.drawable.utils.c
import de.robolab.renderer.utils.DrawContext
import de.westermann.kobserve.event.once
import de.westermann.kobserve.property.mapBinding

class LineView(
        points: List<Point>,
        width: Double,
        color: ViewColor
) : BaseView() {

    val pointsTransition = transition(points)
    val points by pointsTransition
    fun setPoints(points: List<Point>, duration: Double = animationTime, offset: Double = 0.0) {
        pointsTransition.animate(points, duration, offset)
    }

    val source: Point
        get() = points.first()

    val target: Point
        get() = points.last()

    val widthTransition = transition(width)
    val width by widthTransition
    fun setWidth(width: Double, duration: Double = animationTime, offset: Double = 0.0) {
        widthTransition.animate(width, duration, offset)
    }

    val colorTransition = transition(color)
    val color by colorTransition
    fun setColor(color: ViewColor, duration: Double = animationTime, offset: Double = 0.0) {
        colorTransition.animate(color, duration, offset)
    }

    val progressTransition = transition(0.0)
    val progress by progressTransition
    fun setProgress(progress: Double, duration: Double = animationTime, offset: Double = 0.0) {
        progressTransition.animate(progress, duration, offset)
    }

    private val length by pointsTransition.mapBinding {
        it.windowed(2, 1).sumByDouble { (p1, p2) -> p1 distanceTo p2 }
    }

    private fun calcLinePoints(): List<Point> {
        return if (progress == 1.0 || progress == -1.0) {
            points
        } else if (progress > 0.0) {
            val result = mutableListOf(points.first())
            val targetLength = length * progress

            var currentLength = 0.0
            for (point in points.drop(1)) {
                val segmentLength = result.last() distanceTo point

                if (currentLength + segmentLength > targetLength) {
                    val nextLength = currentLength + segmentLength
                    val p = (targetLength - currentLength) / (nextLength - currentLength)
                    result += result.last().interpolate(point, p)
                    break
                } else {
                    currentLength += segmentLength
                    result += point
                }
            }

            result
        } else {
            val result = mutableListOf(points.last())
            val targetLength = length * (1 - progress)

            var currentLength = 0.0
            for (point in points.dropLast(1).asReversed()) {
                val segmentLength = result.last() distanceTo point

                if (currentLength + segmentLength > targetLength) {
                    val nextLength = currentLength + segmentLength
                    val p = (targetLength - currentLength) / (nextLength - currentLength)
                    result += result.last().interpolate(point, p)
                    break
                } else {
                    currentLength += segmentLength
                    result += point
                }
            }

            result
        }
    }

    override fun onDraw(context: DrawContext) {
        if (progress == 0.0) return

        val points = calcLinePoints()
        context.strokeLine(points, context.c(color), width)

        super.onDraw(context)
    }

    override fun calculateBoundingBox(): Rectangle? {
        val parentBox = super.calculateBoundingBox()
        return Rectangle.fromEdges(points).expand(width) unionNullable parentBox
    }

    fun getNearestPointOnLine(point: Point): Point {
         val mappedPoints =  calcLinePoints().windowed(2, 1).map {(source,target) ->
            val lineVec = target - source
            val pointVec = point - source

            val (distance, projection) = pointVec projectOnto lineVec

            val normLinePoint = when {
                distance < 0.0 -> source
                distance > 1.0 -> target
                else -> projection + source
            }

            normLinePoint
        }
        
        return mappedPoints.minBy { it distanceTo point }!!
    }
    
    override fun checkPoint(planetPoint: Point, canvasPoint: Point, epsilon: Double): Boolean {
        val linePoints = calcLinePoints()
        
        for ((source,target) in linePoints.windowed(2, 1)) {
            val lineVec = target - source
            val pointVec = planetPoint - source

            val (distance, projection) = pointVec projectOnto lineVec

            val lineDistance = when {
                distance < 0.0 -> source
                distance > 1.0 -> target
                else -> projection + source
            } distanceTo planetPoint

            if (lineDistance - epsilon < width / 2) {
                return true
            }
        }

        return false
    }

    override fun onCreate() {
        setProgress(1.0)
    }

    override fun onDestroy(onFinish: () -> Unit) {
        setProgress(0.0)

        animatableManager.onFinish(onFinish)
    }
}
