package de.robolab.renderer.document

import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.data.unionNullable
import de.robolab.renderer.document.base.BaseView
import de.robolab.renderer.drawable.utils.c
import de.robolab.renderer.utils.DrawContext
import de.westermann.kobserve.event.once

class ArrowView(
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

    val sizeFactorTransition = transition(0.0)
    val sizeFactor by sizeFactorTransition
    fun setSizeFactor(sizeFactor: Double, duration: Double = animationTime, offset: Double = 0.0) {
        sizeFactorTransition.animate(sizeFactor, duration, offset)
    }


    override fun onDraw(context: DrawContext) {
        if (sizeFactor == 0.0) return

        val scaledSource: Point
        val scaledTarget: Point
        val scaledWidth: Double

        if (sizeFactor < 1.0) {
            scaledSource = source.interpolate(target, 0.5 + sizeFactor / 2)
            scaledTarget = target.interpolate(source, 0.5 + sizeFactor / 2)
            scaledWidth = width * sizeFactor
        } else {
            scaledSource = source
            scaledTarget = target
            scaledWidth = width
        }
        val c = context.c(color)

        context.strokeLine(
                listOf(scaledSource, scaledTarget.interpolate(scaledSource, 0.3)),
                c,
                scaledWidth
        )

        val arrowMiddle = scaledTarget.interpolate(scaledSource, 0.4)
        val vector = (arrowMiddle - scaledTarget) * 0.7
        val left = arrowMiddle + Point(vector.top, -vector.left)
        val right = arrowMiddle + Point(-vector.top, vector.left)
        context.fillPolygon(
                listOf(scaledTarget, left, right),
                c
        )

        super.onDraw(context)
    }

    override fun calculateBoundingBox(): Rectangle? {
        val parentBox = super.calculateBoundingBox()
        return Rectangle.fromEdges(source, target).expand(width) unionNullable parentBox
    }
    
    override fun checkPoint(planetPoint: Point, canvasPoint: Point, epsilon: Double): Boolean {
        val source = source.interpolate(target, 0.5 + sizeFactor / 2)
        val target = target.interpolate(source, 0.5 + sizeFactor / 2)

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
        setSizeFactor(1.0)
    }

    override fun onDestroy(onFinish: () -> Unit) {
        setSizeFactor(0.0)
        setColor(ViewColor.TRANSPARENT)

        animatableManager.onFinish(onFinish)
    }
}
