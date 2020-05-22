package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.drawable.utils.c
import de.robolab.client.renderer.transition.ValueTransition
import de.robolab.client.renderer.transition.nullableInterpolator
import de.robolab.client.renderer.view.base.BaseView
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.common.utils.Point
import de.robolab.common.utils.Rectangle
import de.robolab.common.utils.unionNullable

class RectangleView(
    private val initRectangle: Rectangle?,
    color: ViewColor
) : BaseView() {

    private val rectangleTransition = transition(null, nullableInterpolator<Rectangle>())
    
    val rectangle by rectangleTransition
    fun setRectangle(rectangle: Rectangle?, duration: Double = animationTime, offset: Double = 0.0) {
        rectangleTransition.animate(rectangle, duration, offset)
    }

    private val colorTransition = ValueTransition(color)
    val color by colorTransition
    fun setColor(color: ViewColor, duration: Double = animationTime, offset: Double = 0.0) {
        colorTransition.animate(color, duration, offset)
    }

    override fun onDraw(context: DrawContext) {
        val rect = rectangle ?: return
        context.fillRect(rect, context.c(color))

        super.onDraw(context)
    }

    override fun calculateBoundingBox(): Rectangle? {
        val parentBox = super.calculateBoundingBox()
        return rectangle unionNullable parentBox
    }
    
    override fun checkPoint(planetPoint: Point, canvasPoint: Point, epsilon: Double): Boolean {
        val rect = rectangle ?: return false
        return planetPoint in rect
    }

    override fun onCreate() {
        setRectangle(initRectangle)
    }

    override fun onDestroy(onFinish: () -> Unit) {
        setRectangle(null)
        setColor(ViewColor.TRANSPARENT)

        animatableManager.onFinish(onFinish)
    }
}
