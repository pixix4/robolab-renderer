package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.drawable.general.viewColor
import de.robolab.client.renderer.drawable.utils.SenderGrouping
import de.robolab.client.renderer.drawable.utils.c
import de.robolab.client.renderer.view.base.BaseView
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.client.theme.utils.intensity
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.utils.Color
import de.robolab.common.utils.Vector
import kotlin.math.PI
import kotlin.math.abs

class SenderCharView(
    center: Vector,
    direction: Vector,
    groupings: List<SenderGrouping>
) : BaseView() {

    constructor(center: Vector, grouping: SenderGrouping): this(center, Vector.ONE, listOf(grouping))

    private val centerTransition = transition(center)
    val center by centerTransition
    fun setCenter(center: Vector, duration: Double = animationTime, offset: Double = 0.0) {
        centerTransition.animate(center, duration, offset)
    }

    private val directionTransition = transition(direction)
    val direction by directionTransition
    fun setDirection(direction: Vector, duration: Double = animationTime, offset: Double = 0.0) {
        directionTransition.animate(direction, duration, offset)
    }

    private var groupings: List<SenderGrouping> = emptyList()
    private var groupingPositions: List<Vector> = emptyList()
    fun setGroupings(groupings: List<SenderGrouping>) {
        this.groupings = groupings
        updatePositions()

        requestRedraw()
    }
    fun setGrouping(grouping: SenderGrouping) = setGroupings(listOf(grouping))

    private fun updatePositions() {
        groupingPositions = (1..groupings.size).map {
            it - (1.0 + groupings.size) / 2.0
        }.map {
            center + (direction.normalize() * POINT_OFFSET * it)
        }
    }

    init {
        setGroupings(groupings)

        centerTransition.onChange {
            updatePositions()
        }
        directionTransition.onChange {
            updatePositions()
        }
    }

    override fun onDraw(context: DrawContext) {
        for ((grouping, position) in groupings.zip(groupingPositions)) {
            draw(
                context,
                position,
                grouping.viewColor,
                grouping.char,
                grouping.changes
            )
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
        const val POINT_RADIUS = 0.07
        const val POINT_OFFSET = POINT_RADIUS * 2.5

        fun draw(context: DrawContext, position: Vector, color: ViewColor, char: Char, border: Boolean = false) {
            if (PreferenceStorage.renderSenderGrouping) {
                val c = context.c(color)
                val cBackground = if (border) c.interpolate(context.theme.plotter.primaryBackgroundColor, 0.6) else c
                val b1 = context.theme.plotter.primaryBackgroundColor
                val b2 = context.theme.plotter.lineColor
                val textColor = (if (b1.contrast(cBackground) * 1.5 > b2.contrast(cBackground)) b1 else b2).a(c.alpha)

                context.fillArc(
                    position,
                    POINT_RADIUS,
                    0.0,
                    2 * PI,
                    cBackground
                )

                if (border) {
                    context.strokeArc(
                        position,
                        POINT_RADIUS + 0.01,
                        0.0,
                        2 * PI,
                        c,
                        0.025
                    )
                }

                context.fillText(
                    char.toString(),
                    position,
                    textColor,
                    alignment = ICanvas.FontAlignment.CENTER
                )

                if (border) {
                    context.strokeLine(listOf(
                        position + Vector(-0.03, -0.05),
                        position + Vector(0.03, -0.05),
                    ), textColor, 0.008)
                }
            }
        }
    }
}

private fun Color.contrast(c: Color): Double {
    return abs(luminance() - c.luminance())
}
