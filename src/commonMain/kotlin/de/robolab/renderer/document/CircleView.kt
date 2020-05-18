package de.robolab.renderer.document

import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.data.unionNullable
import de.robolab.renderer.document.base.BaseView
import de.robolab.renderer.drawable.utils.c
import de.robolab.renderer.utils.DrawContext
import de.westermann.kobserve.event.once
import kotlin.math.PI

class CircleView(
        center: Point,
        private val initRadius: Double,
        color: ViewColor
) : BaseView() {


    val centerTransition = transition(center)
    val center by centerTransition
    fun setCenter(center: Point, duration: Double = animationTime, offset: Double = 0.0) {
        centerTransition.animate(center, duration, offset)
    }

    val radiusTransition = transition(0.0)
    val radius by radiusTransition
    fun setRadius(radius: Double, duration: Double = animationTime, offset: Double = 0.0) {
        radiusTransition.animate(radius, duration, offset)
    }

    val colorTransition = transition(color)
    val color by colorTransition
    fun setColor(color: ViewColor, duration: Double = animationTime, offset: Double = 0.0) {
        colorTransition.animate(color, duration, offset)
    }


    override fun onDraw(context: DrawContext) {
        if (radius == 0.0) return

        context.fillArc(center, radius, 0.0, 2.0 * PI, context.c(color))

        super.onDraw(context)
    }

    override fun updateBoundingBox(): Rectangle? {
        val parentBox = super.updateBoundingBox()
        return Rectangle(
                center.left - radius,
                center.top - radius,
                2 * radius,
                2 * radius
        ) unionNullable parentBox
    }

    override fun checkPoint(planetPoint: Point, canvasPoint: Point, epsilon: Double): Boolean {
        return center.distanceTo(planetPoint) <= radius
    }

    override fun onCreate() {
        setRadius(initRadius)
    }

    override fun onDestroy(onFinish: () -> Unit) {
        onAnimationFinish.once {
            onFinish()
        }

        setRadius(0.0)
    }
}
