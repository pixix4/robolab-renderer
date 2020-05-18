package de.robolab.renderer.document

import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.data.unionNullable
import de.robolab.renderer.document.base.BaseView
import de.robolab.renderer.drawable.utils.c
import de.robolab.renderer.utils.DrawContext
import de.westermann.kobserve.event.once

class LineView(
        source: Point,
        target: Point,
        width: Double,
        color: ViewColor
) : BaseView() {

    val sourceTransition = transition(source)
    val source by sourceTransition
    fun setSource(source: Point, duration: Double = animationTime, offset: Double = 0.0) {
        sourceTransition.animate(source, duration, offset)
    }

    val targetTransition = transition(target)
    val target by targetTransition
    fun setTarget(target: Point, duration: Double = animationTime, offset: Double = 0.0) {
        targetTransition.animate(target, duration, offset)
    }

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

    private fun calcLinePoints(): List<Point> {
        return if (progress == 1.0 || progress == -1.0) {
            listOf(source, target)
        } else if (progress > 0.0) {
            listOf(
                    source,
                    source.interpolate(target, progress)
            )
        } else {
            listOf(
                    target,
                    target.interpolate(source, -progress)
            )
        }
    }
    
    override fun onDraw(context: DrawContext) {
        if (progress == 0.0) return

        val points = calcLinePoints()
        context.strokeLine(points, context.c(color), width)

        super.onDraw(context)
    }

    override fun updateBoundingBox(): Rectangle? {
        val parentBox = super.updateBoundingBox()
        return Rectangle.fromEdges(source, target).expand(width) unionNullable parentBox
    }

    override fun checkPoint(planetPoint: Point, canvasPoint: Point, epsilon: Double): Boolean {
        val (source, target) = calcLinePoints()

        val lineVec = target - source
        val pointVec = planetPoint - source

        val (distance, projection) = pointVec projectOnto lineVec

        return when {
            distance < 0.0 -> source
            distance > 1.0 -> target
            else -> projection
        }.distanceTo(planetPoint) - epsilon < width / 2
    }


    override fun onCreate() {
        setProgress(1.0)
    }

    override fun onDestroy(onFinish: () -> Unit) {
        onAnimationFinish.once {
            onFinish()
        }

        setProgress(0.0)
    }
}
