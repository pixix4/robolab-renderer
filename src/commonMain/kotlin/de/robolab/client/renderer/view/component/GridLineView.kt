package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.view.base.BaseView
import de.robolab.common.utils.Vector
import kotlin.math.ceil
import kotlin.math.floor

class GridLineView : BaseView() {

    override fun onDraw(context: DrawContext) {
        val rectangle = context.area
        val startTop = ceil(rectangle.top).toInt()
        val stopTop = floor(rectangle.bottom).toInt()

        for (top in startTop..stopTop) {
            val alpha = GridNumberView.alphaOfLine(top, context.transformation.scaledGridWidth)
            if (alpha == 0.0) continue

            val p1 = context.transformation.planetToCanvas(Vector(rectangle.left, top.toDouble()))
            val p2 = context.transformation.planetToCanvas(Vector(rectangle.right, top.toDouble()))

            context.canvas.strokeLine(
                    listOf(
                            p1,
                            p2
                    ),
                    context.theme.plotter.gridColor.a(alpha),
                    1.0
            )
        }

        val startLeft = ceil(rectangle.left).toInt()
        val stopLeft = floor(rectangle.right).toInt()

        for (left in startLeft..stopLeft) {
            val alpha = GridNumberView.alphaOfLine(left, context.transformation.scaledGridWidth)
            if (alpha == 0.0) continue

            val p1 = context.transformation.planetToCanvas(Vector(left.toDouble(), rectangle.top))
            val p2 = context.transformation.planetToCanvas(Vector(left.toDouble(), rectangle.bottom))

            context.canvas.strokeLine(
                    listOf(
                            p1,
                            p2
                    ),
                    context.theme.plotter.gridColor.a(alpha),
                    1.0
            )
        }
    }

    override fun checkPoint(planetPoint: Vector, canvasPoint: Vector, epsilon: Double): Boolean {
        return false
    }
}
