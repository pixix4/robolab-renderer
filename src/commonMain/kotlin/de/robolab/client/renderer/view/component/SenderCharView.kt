package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.drawable.general.viewColor
import de.robolab.client.renderer.drawable.utils.SenderGrouping
import de.robolab.client.renderer.drawable.utils.c
import de.robolab.client.renderer.view.base.BaseView
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.utils.Point
import kotlin.math.PI

class SenderCharView(
    center: Point,
    initGrouping: SenderGrouping?
) : BaseView() {

    private val centerTransition = transition(center)
    val center by centerTransition
    fun setCenter(center: Point, duration: Double = animationTime, offset: Double = 0.0) {
        centerTransition.animate(center, duration, offset)
    }

    private var oldGrouping = initGrouping
    private var newGrouping = initGrouping

    private val progressTransition = transition(0.0)
    val progress by progressTransition
    fun setGrouping(grouping: SenderGrouping?, duration: Double = animationTime, offset: Double = 0.0) {
        oldGrouping = newGrouping
        newGrouping = grouping

        progressTransition.resetValue(0.0)
        progressTransition.animate(1.0, duration, offset)
    }


    override fun onDraw(context: DrawContext) {
        val new = newGrouping
        val old = oldGrouping
        if (new != null) {
            if (old != null) {
                draw(
                    context,
                    center,
                    new.viewColor.interpolate(old.viewColor, progress),
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
            if (old != null) {
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

    override fun checkPoint(planetPoint: Point, canvasPoint: Point, epsilon: Double): Boolean {
        return false
    }

    override fun debugStringParameter(): List<Any?> {
        return listOf(center)
    }

    companion object {
        fun draw(context: DrawContext, position: Point, color: ViewColor, char: Char) {
            if (PreferenceStorage.renderSenderGrouping) {
                val c = context.c(color)
                context.strokeArc(
                    position,
                    0.07,
                    0.0,
                    2 * PI,
                    c,
                    PlottingConstraints.LINE_WIDTH / 2
                )
                context.fillText(
                    char.toString(),
                    position,
                    c,
                    alignment = ICanvas.FontAlignment.CENTER
                )
            }
        }
    }
}
