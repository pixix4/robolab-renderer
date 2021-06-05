package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.drawable.utils.c
import de.robolab.client.renderer.view.base.BaseView
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.common.utils.Vector
import de.robolab.common.utils.Rectangle
import de.robolab.common.utils.unionNullable
import kotlin.math.PI

class CircleView(
    center: Vector,
    private val initRadius: Double,
    color: ViewColor
) : BaseView() {


    val centerTransition = transition(center)
    val center by centerTransition
    fun setCenter(center: Vector, duration: Double = animationTime, offset: Double = 0.0) {
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

    override fun calculateBoundingBox(): Rectangle? {
        val parentBox = super.calculateBoundingBox()
        return Rectangle(
            center.left - radius,
            center.top - radius,
            2 * radius,
            2 * radius
        ) unionNullable parentBox
    }

    override fun checkPoint(planetPoint: Vector, canvasPoint: Vector, epsilon: Double): Boolean {
        return center.distanceTo(planetPoint) <= radius
    }

    override fun onCreate() {
        setRadius(initRadius)
    }

    override fun onDestroy(onFinish: () -> Unit) {
        setRadius(0.0)

        animatableManager.onFinish(onFinish)
    }
}
