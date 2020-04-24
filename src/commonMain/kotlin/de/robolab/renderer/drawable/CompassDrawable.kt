package de.robolab.renderer.drawable

import de.robolab.renderer.ITransformationReference
import de.robolab.renderer.TransformationInteraction
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.platform.PointerEvent
import de.robolab.renderer.utils.DrawContext
import de.robolab.renderer.utils.Transformation
import kotlin.math.PI
import kotlin.math.round

class CompassDrawable(private val transformationReference: ITransformationReference) : IDrawable {

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
                context.theme.plotter.lineColor.a(0.25)
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

        context.canvas.fillPolygon(arrowTop, context.theme.plotter.redColor)
        context.canvas.fillPolygon(arrowBottom, context.theme.plotter.lineColor.a(0.8))
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        return emptyList()
    }

    override fun onPointerDown(event: PointerEvent, position: Point): Boolean {
        val compassCenter = Point(
                event.screen.width - RIGHT_PADDING,
                TOP_PADDING
        )

        return event.point.distance(compassCenter) <= RADIUS
    }

    override fun onPointerUp(event: PointerEvent, position: Point): Boolean {
        val compassCenter = Point(
                event.screen.width - RIGHT_PADDING,
                TOP_PADDING
        )

        if (event.point.distance(compassCenter) <= RADIUS) {
            val currentAngle = round(transformation.rotation / PI * 180.0 * 100.0) / 100.0
            val newAngle = ((currentAngle - 180.0) % 360.0 + 180.0) % 360.0
            transformationReference.transformation?.rotateTo(newAngle / 180.0 * PI, event.screen / 2)
            if (newAngle != 0.0) {
                transformationReference.transformation?.rotateTo(0.0, event.screen / 2, 250.0)
            } else {
                transformationReference.autoCentering = true
                transformationReference.centerPlanet(TransformationInteraction.ANIMATION_TIME)
            }
            return true
        }

        return false
    }

    companion object {
        const val RADIUS = 24.0

        const val TOP_PADDING = 20.0 + RADIUS
        const val RIGHT_PADDING = 20.0 + RADIUS
    }
}
