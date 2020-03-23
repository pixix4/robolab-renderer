package de.robolab.renderer.drawable

import de.robolab.renderer.DrawContext
import de.robolab.renderer.Transformation
import de.robolab.renderer.data.Point
import kotlin.math.PI

object CompassDrawable : IDrawable {

    override fun onUpdate(ms_offset: Double): Boolean {
        return false
    }

    private val transformation = Transformation()

    override fun onDraw(context: DrawContext) {
        val center = Point(context.width - RIGHT_PADDING, TOP_PADDING)
        context.canvas.fillArc(
                center,
                RADIUS,
                0.0,
                2 * PI,
                context.theme.lineColor.a(0.25)
        )

        transformation.setTranslation(center)
        transformation.setScaleFactor(0.2)
        transformation.setRotationAngle(context.transformation.rotation)

        val arrowTop = listOf(
                Point(0.0, 1.0),
                Point(0.4, 0.0),
                Point(-0.4, 0.0)
        ).map(transformation::planetToCanvas)

        val arrowBottom = listOf(
                Point(0.0, -1.0),
                Point(0.4, -0.0),
                Point(-0.4, -0.0)
        ).map(transformation::planetToCanvas)

        context.canvas.fillPolygon(arrowTop, context.theme.redColor)
        context.canvas.fillPolygon(arrowBottom, context.theme.lineColor.a(0.8))
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        return emptyList()
    }

    const val RADIUS = 24.0

    const val TOP_PADDING = 20.0 + RADIUS
    const val RIGHT_PADDING = 20.0 + RADIUS
}
