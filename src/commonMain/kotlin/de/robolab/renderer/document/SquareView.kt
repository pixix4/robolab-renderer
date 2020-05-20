package de.robolab.renderer.document

import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.data.unionNullable
import de.robolab.renderer.document.base.BaseView
import de.robolab.renderer.drawable.utils.c
import de.robolab.renderer.utils.DrawContext
import de.westermann.kobserve.event.once
import de.westermann.kobserve.property.property

class SquareView(
        center: Point,
        private val initSize: Double,
        borderWidth: Double,
        color: ViewColor,
        isFilled: Boolean = true
) : BaseView() {

    private val centerTransition = transition(center)
    val center by centerTransition
    fun setCenter(center: Point, duration: Double = animationTime, offset: Double = 0.0) {
        centerTransition.animate(center, duration, offset)
    }

    private val sizeTransition = transition(0.0)
    val squareSize by sizeTransition
    fun setSize(size: Double, duration: Double = animationTime, offset: Double = 0.0) {
        sizeTransition.animate(size, duration, offset)
    }

    private val borderWidthTransition = transition(borderWidth)
    val borderWidth by borderWidthTransition
    fun setBorderWidth(borderWidth: Double, duration: Double = animationTime, offset: Double = 0.0) {
        borderWidthTransition.animate(borderWidth, duration, offset)
    }

    private val colorTransition = transition(color)
    val color by colorTransition
    fun setColor(color: ViewColor, duration: Double = animationTime, offset: Double = 0.0) {
        colorTransition.animate(color, duration, offset)
    }

    private val filledFactorTransition = transition(if (isFilled) 1.0 else 0.0)
    val filledFactor by filledFactorTransition
    fun setIsFilled(isFilled: Boolean, duration: Double = animationTime, offset: Double = 0.0) {
        filledFactorTransition.animate(if (isFilled) 1.0 else 0.0, duration, offset)
    }

    private val rect by property(centerTransition, sizeTransition) {
        Rectangle(
                center.left - squareSize / 2,
                center.top - squareSize / 2,
                squareSize,
                squareSize
        )
    }

    override fun onDraw(context: DrawContext) {
        if (filledFactor < 1.0) {
            val innerRect = rect.shrink(borderWidth / 2)
            context.strokeRect(innerRect, context.c(color), borderWidth)

            if (filledFactor > 0.0) {
                val strokeWidth = innerRect.width * filledFactor
                context.strokeRect(innerRect.shrink(strokeWidth / 2), context.c(color), strokeWidth)
            }
        } else {
            context.fillRect(rect, context.c(color))
        }

        super.onDraw(context)
    }

    override fun calculateBoundingBox(): Rectangle? {
        val parentBox = super.calculateBoundingBox()
        return rect unionNullable parentBox
    }
    
    override fun checkPoint(planetPoint: Point, canvasPoint: Point, epsilon: Double): Boolean {
        return planetPoint in rect
    }

    override fun onCreate() {
        setSize(initSize)
    }

    override fun onDestroy(onFinish: () -> Unit) {
        setSize(0.0)

        animatableManager.onFinish(onFinish)
    }
}
