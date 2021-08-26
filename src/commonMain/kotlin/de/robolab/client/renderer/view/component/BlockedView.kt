package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.drawable.utils.c
import de.robolab.client.renderer.view.base.BaseView
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.common.utils.Vector
import de.westermann.kobserve.property.mapBinding
import kotlin.math.PI

class BlockedView(
    center: Vector,
    color: ViewColor,
    isPartial: Boolean
) : BaseView() {

    private val centerTransition = transition(center)
    val center by centerTransition
    fun setCenter(center: Vector, duration: Double = animationTime, offset: Double = 0.0) {
        centerTransition.animate(center, duration, offset)
    }

    private val line by centerTransition.mapBinding {
        listOf(
            it - Vector(0.045, 0),
            it + Vector(0.045, 0),
        )
    }

    val colorTransition = transition(color)
    val color by colorTransition
    fun setColor(color: ViewColor, duration: Double = animationTime, offset: Double = 0.0) {
        colorTransition.animate(color, duration, offset)
    }

    private var isPartial: Boolean = isPartial
    fun setIsPartial(isPartial: Boolean) {
        this.isPartial = isPartial
        requestRedraw()
    }

    override fun onDraw(context: DrawContext) {
        val color = context.c(color)

        if (isPartial) {
            context.fillArc(
                center,
                0.08,
                0.0,
                2.0 * PI,
                context.c(ViewColor.PRIMARY_BACKGROUND_COLOR).a(0.8)
            )
            context.strokeArc(center, 0.07, 0.0, 2.0 * PI, color.a(0.8), 0.02)
            context.strokeLine(line, color.a(0.8), 0.025)
        } else {
            context.fillArc(center, 0.08, 0.0, 2.0 * PI, color)
            context.strokeLine(line, context.c(ViewColor.PRIMARY_BACKGROUND_COLOR).a(color.alpha), 0.025)
        }

        super.onDraw(context)
    }

    override fun checkPoint(planetPoint: Vector, canvasPoint: Vector, epsilon: Double): Boolean {
        return false
    }

    override fun debugStringParameter(): List<Any?> {
        return listOf(center)
    }

    override fun onDestroy(onFinish: () -> Unit) {
        animatableManager.onFinish(onFinish)
    }
}
