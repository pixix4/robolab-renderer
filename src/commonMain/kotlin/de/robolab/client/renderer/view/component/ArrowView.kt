package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.drawable.utils.c
import de.robolab.client.renderer.view.base.BaseView
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.common.utils.Color
import de.robolab.common.utils.Point
import de.robolab.common.utils.Rectangle
import de.robolab.common.utils.unionNullable

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
            scaledSource = target.interpolate(source, 0.5 + sizeFactor / 2)
            scaledTarget = source.interpolate(target, 0.5 + sizeFactor / 2)
            scaledWidth = width * sizeFactor
        } else {
            scaledSource = source
            scaledTarget = target
            scaledWidth = width
        }

        draw(context, scaledSource, scaledTarget, scaledWidth, color)

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

    companion object {
        fun draw(
            context: DrawContext,
            source: Point,
            target: Point,
            width: Double,
            color: ViewColor
        ) {
            val c = context.c(color)
            context.strokeLine(
                listOf(source, target.interpolate(source, 0.3)),
                c,
                width
            )

            val arrowMiddle = target.interpolate(source, 0.4)
            val vector = (arrowMiddle - target) * 0.7
            val left = arrowMiddle + Point(vector.top, -vector.left)
            val right = arrowMiddle + Point(-vector.top, vector.left)
            context.fillPolygon(
                listOf(target, left, right),
                c
            )
        }
    }
}
