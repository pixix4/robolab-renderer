package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.PlottingConstraints
import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.drawable.utils.SenderGrouping
import de.robolab.client.renderer.drawable.utils.c
import de.robolab.client.renderer.view.base.BaseView
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.common.utils.Vector
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin

class SenderView(
    center: Vector,
    private val initColors: List<Pair<SenderGrouping, List<Vector>>>,
) : BaseView() {

    private val centerTransition = transition(center)
    val center by centerTransition
    fun setCenter(center: Vector, duration: Double = animationTime, offset: Double = 0.0) {
        centerTransition.animate(center, duration, offset)
    }

    private var oldColors: List<Pair<SenderGrouping, List<Vector>>> = emptyList()
    private var newColors: List<Pair<SenderGrouping, List<Vector>>> = emptyList()

    private val progressTransition = transition(0.0)
    val progress by progressTransition
    fun setColors(
        colors: List<Pair<SenderGrouping, List<Vector>>>,
        duration: Double = animationTime,
        offset: Double = 0.0
    ) {
        oldColors = newColors
        newColors = colors

        progressTransition.resetValue(0.0)
        progressTransition.animate(1.0, duration, offset)
    }

    override fun onDraw(context: DrawContext) {
        val length = max(newColors.size, oldColors.size)

        for (index in 0..length) {
            val p = when (index % 4) {
                0 -> Vector(PlottingConstraints.POINT_SIZE / 2.5, PlottingConstraints.POINT_SIZE / 2.5)
                1 -> Vector(-PlottingConstraints.POINT_SIZE / 2.5, PlottingConstraints.POINT_SIZE / 2.5)
                2 -> Vector(-PlottingConstraints.POINT_SIZE / 2.5, -PlottingConstraints.POINT_SIZE / 2.5)
                3 -> Vector(PlottingConstraints.POINT_SIZE / 2.5, -PlottingConstraints.POINT_SIZE / 2.5)
                else -> Vector(0.0, 0.0)
            }

            val newGroup = newColors.getOrNull(index)
            val oldGroup = oldColors.getOrNull(index)

            val steps = (length - (index % 4)) / 4 + 1
            val step = index / 4
            val extend = (PI / 2 - step * PI / (steps * 2.0))

            if (newGroup != null) {
                val newColor = newGroup.first.color.let { ViewColor.c(it) }
                if (oldGroup != null) {
                    val oldColor = oldGroup.first.color.let { ViewColor.c(it) }
                    satellite(
                        context,
                        center + p,
                        index * PI / 2,
                        extend,
                        oldColor.interpolate(newColor, progress),
                        if (progress < 0.5) oldGroup.first.char else newGroup.first.char,
                        if (progress < 0.5) oldGroup.second else newGroup.second,
                        1.0
                    )
                } else {
                    satellite(
                        context,
                        center + p,
                        index * PI / 2,
                        extend * progress,
                        newColor,
                        newGroup.first.char,
                        newGroup.second,
                        progress
                    )
                }
            } else if (oldGroup != null) {
                val oldColor = oldGroup.first.color.let { ViewColor.c(it) }
                satellite(
                    context,
                    center + p,
                    index * PI / 2,
                    extend * (1.0 - progress),
                    oldColor,
                    oldGroup.first.char,
                    oldGroup.second,
                    1.0 - progress
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

    override fun onCreate() {
        setColors(initColors)
    }

    override fun onDestroy(onFinish: () -> Unit) {
        setColors(emptyList())

        animatableManager.onFinish(onFinish)
    }

    companion object {

        fun satellite(
            context: DrawContext,
            position: Vector,
            start: Double,
            extend: Double,
            color: ViewColor,
            char: Char?,
            directions: List<Vector>,
            alpha: Double,
            scale: Double = 1.0
        ) {
            val c = context.c(color)
            context.strokeArc(
                position,
                PlottingConstraints.TARGET_RADIUS * 0.4 * scale,
                start,
                extend,
                c,
                PlottingConstraints.LINE_WIDTH * scale
            )
            context.strokeArc(
                position,
                PlottingConstraints.TARGET_RADIUS * 0.7 * scale,
                start,
                extend,
                c,
                PlottingConstraints.LINE_WIDTH * scale
            )
            context.strokeArc(
                position,
                PlottingConstraints.TARGET_RADIUS * 1.0 * scale,
                start,
                extend,
                c,
                PlottingConstraints.LINE_WIDTH * scale
            )

            if (char != null) {
                val center = position + Vector(PlottingConstraints.TARGET_RADIUS * 1.4 * scale, 0.0).rotate(start + extend / 2)
                val cc = if (alpha < 1.0) ViewColor.TRANSPARENT.interpolate(color, alpha) else color

                val r = 0.07 * scale
                val a = PI / 3
                val b = PI / 2 - a / 2
                val h = r / sin(a / 2)

                for (d in directions) {
                    val t = (d - center).normalize() * r

                    val points = listOf(
                        center + t.rotate(b),
                        center + t.rotate(-b),
                        center + t.normalize() * h
                    )

                    context.fillPolygon(
                        points,
                        context.c(cc)
                    )
                }

                SenderCharView.draw(context, center, cc, char)
            }
        }
    }
}
