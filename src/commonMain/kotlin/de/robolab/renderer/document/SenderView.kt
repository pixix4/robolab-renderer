package de.robolab.renderer.document

import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.document.base.BaseView
import de.robolab.renderer.drawable.utils.c
import de.robolab.renderer.utils.DrawContext
import de.westermann.kobserve.event.once
import kotlin.math.PI
import kotlin.math.max

class SenderView(
        center: Point,
        private val initColors: List<ViewColor>
) : BaseView() {

    private val centerTransition = transition(center)
    val center by centerTransition
    fun setCenter(center: Point, duration: Double = animationTime, offset: Double = 0.0) {
        centerTransition.animate(center, duration, offset)
    }

    private var oldColors: List<ViewColor> = emptyList()
    private var newColors: List<ViewColor> = emptyList()

    private val progressTransition = transition(0.0)
    val progress by progressTransition
    fun setColors(colors: List<ViewColor>, duration: Double = animationTime, offset: Double = 0.0) {
        oldColors = newColors
        newColors = colors

        progressTransition.resetValue(0.0)
        progressTransition.animate(1.0, duration, offset)
    }

    private fun satellite(context: DrawContext, position: Point, start: Double, extend: Double = 90.0, color: ViewColor) {
        val c = context.c(color)
        context.strokeArc(position, PlottingConstraints.TARGET_RADIUS * 0.4, start, extend, c, PlottingConstraints.LINE_WIDTH)
        context.strokeArc(position, PlottingConstraints.TARGET_RADIUS * 0.7, start, extend, c, PlottingConstraints.LINE_WIDTH)
        context.strokeArc(position, PlottingConstraints.TARGET_RADIUS * 1.0, start, extend, c, PlottingConstraints.LINE_WIDTH)
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

            val newColor = newColors.getOrNull(index)
            val oldColor = oldColors.getOrNull(index)

            val steps = (length - (index % 4)) / 4 + 1
            val step = index / 4
            val extend = (PI / 2 - step * PI / (steps * 2.0))

            if (newColor != null) {
                if (oldColor != null) {
                    satellite(context, center + p, index * PI / 2, extend, oldColor.interpolate(newColor, progress))
                } else {
                    satellite(context, center + p, index * PI / 2, extend * progress, newColor)
                }
            } else if (oldColor != null) {
                satellite(context, center + p, index * PI / 2, extend * (1.0 - progress), oldColor)
            }
        }

        super.onDraw(context)
    }

    override fun calculateBoundingBox(): Rectangle? {
        val parentBox = super.calculateBoundingBox()
        // TODO
        return null
    }

    override fun checkPoint(planetPoint: Point, canvasPoint: Point, epsilon: Double): Boolean {
        return false
    }

    override fun onCreate() {
        setColors(initColors)
    }

    override fun onDestroy(onFinish: () -> Unit) {
        setColors(emptyList())

        animatableManager.onFinish(onFinish)
    }
}