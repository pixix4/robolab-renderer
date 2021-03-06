package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.drawable.general.viewColor
import de.robolab.client.renderer.drawable.utils.SenderGrouping
import de.robolab.client.renderer.drawable.utils.c
import de.robolab.client.renderer.view.base.BaseView
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.utils.Color
import de.robolab.common.utils.Vector
import kotlin.math.PI
import kotlin.math.abs

class SenderCharView(
    center: Vector,
    initGrouping: SenderGrouping?
) : BaseView() {

    private val centerTransition = transition(center)
    val center by centerTransition
    fun setCenter(center: Vector, duration: Double = animationTime, offset: Double = 0.0) {
        centerTransition.animate(center, duration, offset)
    }

    private var oldGrouping: SenderGrouping? = null
    private var newGrouping: SenderGrouping? = initGrouping

    private val progressTransition = transition(0.0)
    val progress by progressTransition
    fun setGrouping(grouping: SenderGrouping?, duration: Double = animationTime, offset: Double = 0.0) {
        progressTransition.resetValue(0.0)

        oldGrouping = newGrouping
        newGrouping = grouping

        progressTransition.animate(1.0, duration, offset)
    }

    override fun onCreate() {
        progressTransition.resetValue(0.0)
        progressTransition.animate(1.0, animationTime)
    }

    override fun onDestroy(onFinish: () -> Unit) {
        setGrouping(null)

        animatableManager.onAnimationFinish {
            onFinish()
        }
    }

    override fun onDraw(context: DrawContext) {
        val old = oldGrouping
        val new = newGrouping

        if (new != null) {
            if (progress >= 1.0) {
                draw(context, center, new.viewColor, new.char)
                return
            }

            if (old != null) {
                draw(
                    context,
                    center,
                    old.viewColor.interpolate(new.viewColor, progress),
                    if (progress < 0.5) old.char else new.char
                )
            } else {
                draw(
                    context,
                    center,
                    ViewColor.TRANSPARENT.interpolate(new.viewColor, progress),
                    new.char
                )
            }
        } else {
            if (old != null && progress < 1.0) {
                draw(
                    context,
                    center,
                    old.viewColor.interpolate(ViewColor.TRANSPARENT, progress),
                    old.char
                )
            }
        }
        super.onDraw(context)
    }

    override fun checkPoint(planetPoint: Vector, canvasPoint: Vector, epsilon: Double): Boolean {
        return false
    }

    override fun debugStringParameter(): List<Any?> {
        return listOf(center)
    }

    companion object {
        fun draw(context: DrawContext, position: Vector, color: ViewColor, char: Char) {
            if (PreferenceStorage.renderSenderGrouping) {
                val c = context.c(color)
                val b1 = context.theme.plotter.primaryBackgroundColor
                val b2 = context.theme.plotter.lineColor
                val textColor = (if (b1.contrast(c) * 1.5 > b2.contrast(c)) b1 else b2).a(c.alpha)
                context.fillArc(
                    position,
                    0.07,
                    0.0,
                    2 * PI,
                    c
                )
                context.fillText(
                    char.toString(),
                    position,
                    textColor,
                    alignment = ICanvas.FontAlignment.CENTER
                )
            }
        }
    }
}

private fun Color.contrast(c: Color): Double {
    return abs(luminance() - c.luminance())
}
