package de.robolab.client.renderer.view.component

import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.drawable.utils.log2
import de.robolab.client.renderer.drawable.utils.power2
import de.robolab.client.renderer.view.base.BaseView
import de.robolab.common.utils.Vector
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor

class GridNumberView : BaseView() {

    override fun onDraw(context: DrawContext) {
        val rectangle = context.area
        val isDefaultAxesOrientation = cos(context.transformation.rotation * 2) >= 0.0

        val startTop = ceil(rectangle.top).toInt()
        val stopTop = floor(rectangle.bottom).toInt()
        for (top in startTop..stopTop) {
            val alpha = alphaOfLine(top, context.transformation.scaledGridWidth)
            if (alpha == 0.0) continue

            val (x1, y1) = context.transformation.planetToCanvas(Vector(rectangle.left, top.toDouble()))
            val (x2, y2) = context.transformation.planetToCanvas(Vector(rectangle.right, top.toDouble()))
            val p = if (isDefaultAxesOrientation) {
                val x3 = 30.0
                val y3 = (x3 - x1) / (x2 - x1) * (y2 - y1) + y1

                if (y3 > context.dimension.height - 50) continue

                Vector(x3, y3)
            } else {
                val y3 = context.canvas.dimension.height - 30.0
                val x3 = (y3 - y1) / (y2 - y1) * (x2 - x1) + x1

                if (x3 < 50) continue

                Vector(x3, y3)
            }

            context.canvas.fillText(top.toString(), p, context.theme.plotter.gridTextColor.a(alpha), FONT_SIZE, alignment = ICanvas.FontAlignment.CENTER)
        }

        val startLeft = ceil(rectangle.left).toInt()
        val stopLeft = floor(rectangle.right).toInt()
        for (left in startLeft..stopLeft) {
            val alpha = alphaOfLine(left, context.transformation.scaledGridWidth)
            if (alpha == 0.0) continue

            val (x1, y1) = context.transformation.planetToCanvas(Vector(left.toDouble(), rectangle.top))
            val (x2, y2) = context.transformation.planetToCanvas(Vector(left.toDouble(), rectangle.bottom))
            val p = if (isDefaultAxesOrientation) {
                val y3 = context.canvas.dimension.height - 30.0
                val x3 = (y3 - y1) / (y2 - y1) * (x2 - x1) + x1
                Vector(x3, y3)
            } else {
                val x3 = 30.0
                val y3 = (x3 - x1) / (x2 - x1) * (y2 - y1) + y1
                Vector(x3, y3)
            }

            context.canvas.fillText(left.toString(), p, context.theme.plotter.gridTextColor.a(alpha), FONT_SIZE, alignment = ICanvas.FontAlignment.CENTER)
        }
    }

    override fun checkPoint(planetPoint: Vector, canvasPoint: Vector, epsilon: Double): Boolean {
        return false
    }

    companion object {

        private const val FONT_SIZE = 16.0

        fun alphaOfLine(index: Int, scaledGridWidth: Double): Double {
            val lineCountPerCell = ((FONT_SIZE * 2.0) / scaledGridWidth)
            if (lineCountPerCell <= 1.0) {
                return 1.0
            }

            val lineModulo = power2(log2(ceil(lineCountPerCell).toInt() - 1) + 1)
            if (index % lineModulo == 0) {
                return 1.0
            }

            val prevLineModulo = power2(log2(ceil(lineCountPerCell).toInt() - 1))
            if (index % prevLineModulo != 0) {
                return 0.0
            }

            val lineDiff = lineCountPerCell - prevLineModulo
            if (lineDiff < 0.2) {
                return 1.0 - lineDiff / 0.2
            }

            return 0.0
        }
    }
}
