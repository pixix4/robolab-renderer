package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.drawable.utils.SenderGrouping
import de.robolab.client.renderer.drawable.utils.c
import de.robolab.client.renderer.view.base.BaseView
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.common.utils.Point
import kotlin.math.PI
import kotlin.math.max

class SenderView(
    center: Point,
    private val initColors: List<SenderGrouping>
) : BaseView() {

    private val centerTransition = transition(center)
    val center by centerTransition
    fun setCenter(center: Point, duration: Double = animationTime, offset: Double = 0.0) {
        centerTransition.animate(center, duration, offset)
    }

    private var oldColors: List<SenderGrouping> = emptyList()
    private var newColors: List<SenderGrouping> = emptyList()

    private val progressTransition = transition(0.0)
    val progress by progressTransition
    fun setColors(colors: List<SenderGrouping>, duration: Double = animationTime, offset: Double = 0.0) {
        oldColors = newColors
        newColors = colors

        progressTransition.resetValue(0.0)
        progressTransition.animate(1.0, duration, offset)
    }

    private fun satellite(
        context: DrawContext,
        position: Point,
        start: Double,
        extend: Double = 90.0,
        color: ViewColor,
        char: Char?,
        alpha: Double
    ) {
        val c = context.c(color)
        context.strokeArc(
            position,
            PlottingConstraints.TARGET_RADIUS * 0.4,
            start,
            extend,
            c,
            PlottingConstraints.LINE_WIDTH
        )
        context.strokeArc(
            position,
            PlottingConstraints.TARGET_RADIUS * 0.7,
            start,
            extend,
            c,
            PlottingConstraints.LINE_WIDTH
        )
        context.strokeArc(
            position,
            PlottingConstraints.TARGET_RADIUS * 1.0,
            start,
            extend,
            c,
            PlottingConstraints.LINE_WIDTH
        )

        if (char != null) {
            val center = position + Point(PlottingConstraints.TARGET_RADIUS * 1.4, 0.0).rotate(start + extend / 2)
            val cc = if (alpha < 1.0) ViewColor.TRANSPARENT.interpolate(color, alpha) else color
            SenderCharView.draw(context, center, cc, char)
        }
    }

    override fun onDraw(context: DrawContext) {
        val length = max(newColors.size, oldColors.size)

        for (index in 0..length) {
            val p = when (index % 4) {
                0 -> Point(PlottingConstraints.POINT_SIZE / 2.5, PlottingConstraints.POINT_SIZE / 2.5)
                1 -> Point(-PlottingConstraints.POINT_SIZE / 2.5, PlottingConstraints.POINT_SIZE / 2.5)
                2 -> Point(-PlottingConstraints.POINT_SIZE / 2.5, -PlottingConstraints.POINT_SIZE / 2.5)
                3 -> Point(PlottingConstraints.POINT_SIZE / 2.5, -PlottingConstraints.POINT_SIZE / 2.5)
                else -> Point(0.0, 0.0)
            }

            val newGroup = newColors.getOrNull(index)
            val oldGroup = oldColors.getOrNull(index)


            val steps = (length - (index % 4)) / 4 + 1
            val step = index / 4
            val extend = (PI / 2 - step * PI / (steps * 2.0))

            if (newGroup != null) {
                val newColor = newGroup.color.let { ViewColor.c(it) }
                if (oldGroup != null) {
                    val oldColor = oldGroup.color.let { ViewColor.c(it) }
                    satellite(
                        context,
                        center + p,
                        index * PI / 2,
                        extend,
                        oldColor.interpolate(newColor, progress),
                        if (progress < 0.5) oldGroup.char else newGroup.char,
                        1.0
                    )
                } else {
                    satellite(context, center + p, index * PI / 2, extend * progress, newColor, newGroup.char, progress)
                }
            } else if (oldGroup != null) {
                val oldColor = oldGroup.color.let { ViewColor.c(it) }
                satellite(
                    context,
                    center + p,
                    index * PI / 2,
                    extend * (1.0 - progress),
                    oldColor,
                    oldGroup.char,
                    1.0 - progress
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

    override fun onCreate() {
        setColors(initColors)
    }

    override fun onDestroy(onFinish: () -> Unit) {
        setColors(emptyList())

        animatableManager.onFinish(onFinish)
    }
}
