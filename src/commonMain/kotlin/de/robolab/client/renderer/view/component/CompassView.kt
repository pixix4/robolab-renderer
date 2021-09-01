package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.utils.ITransformation
import de.robolab.client.renderer.utils.Transformation
import de.robolab.client.renderer.view.base.BaseView
import de.robolab.common.utils.Color
import de.robolab.common.utils.Vector
import kotlin.math.PI

class CompassView: BaseView() {

    private val transformation: ITransformation = Transformation()

    private var compassCenter = Vector.ZERO
    override fun onDraw(context: DrawContext) {
        compassCenter = Vector(context.dimension.width - RIGHT_PADDING, TOP_PADDING)
        context.canvas.fillArc(
                compassCenter,
                RADIUS,
                0.0,
                2 * PI,
                context.theme.plotter.lineColor.a(0.25)
        )

        transformation.setTranslation(compassCenter)
        transformation.setScaleFactor(0.2)
        transformation.setRotationAngle(context.transformation.rotation)

        val arrowTop = listOf(
                transformation.planetToCanvas(Vector(0.0, 1.0)),
                transformation.planetToCanvas(Vector(0.4, 0.0)),
                transformation.planetToCanvas(Vector(-0.4, 0.0)),
        )

        val arrowBottom = listOf(
                transformation.planetToCanvas(Vector(0.0, -1.0)),
                transformation.planetToCanvas(Vector(0.4, -0.0)),
                transformation.planetToCanvas(Vector(-0.4, -0.0)),
        )

        context.canvas.fillPolygon(arrowTop, context.theme.plotter.redColor)
        context.canvas.fillPolygon(arrowBottom, context.theme.plotter.lineColor.a(0.8))

        val halfWidth = 0.6
        val diagonalWidth = 2.0
        val leftBottom = transformation.planetToCanvas(Vector(-0.1375, 0.05))
        val leftTop = transformation.planetToCanvas(Vector(-0.1375, 0.45))
        val rightBottom = transformation.planetToCanvas(Vector(0.1375, 0.05))
        val rightTop = transformation.planetToCanvas(Vector(0.1375, 0.45))

        val north = listOf(
            leftBottom + Vector(halfWidth, 0.0).rotate(-transformation.rotation),
            leftBottom + Vector(-halfWidth, 0.0).rotate(-transformation.rotation),
            leftTop + Vector(-halfWidth, 0.0).rotate(-transformation.rotation),
            leftTop + Vector(halfWidth, 0.0).rotate(-transformation.rotation),
            rightBottom + Vector(-halfWidth, -diagonalWidth).rotate(-transformation.rotation),
            rightTop + Vector(-halfWidth, 0.0).rotate(-transformation.rotation),
            rightTop + Vector(halfWidth, 0.0).rotate(-transformation.rotation),
            rightBottom + Vector(halfWidth, 0.0).rotate(-transformation.rotation),
            rightBottom + Vector(-halfWidth, 0.0).rotate(-transformation.rotation),
            leftTop + Vector(halfWidth, diagonalWidth).rotate(-transformation.rotation),
        )

        context.canvas.fillPolygon(north, Color.WHITE)
    }

    override fun checkPoint(planetPoint: Vector, canvasPoint: Vector, epsilon: Double): Boolean {
        return canvasPoint.distanceTo(compassCenter) <= RADIUS
    }

    companion object {
        const val RADIUS = 24.0

        const val TOP_PADDING = 20.0 + RADIUS
        const val RIGHT_PADDING = 20.0 + RADIUS
    }
}
