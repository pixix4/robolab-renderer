package de.robolab.renderer.drawable

import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.base.IDrawable
import de.robolab.renderer.drawable.utils.log2
import de.robolab.renderer.drawable.utils.power2
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.utils.DrawContext
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor

object GridNumbersDrawable : IDrawable {

    override fun onUpdate(ms_offset: Double): Boolean {
        return false
    }

    override fun onDraw(context: DrawContext) {
        val rectangle = context.area
        val isDefaultAxesOrientation = cos(context.transformation.rotation * 2) >= 0.0

        val startTop = ceil(rectangle.top).toInt()
        val stopTop = floor(rectangle.bottom).toInt()
        for (top in startTop..stopTop) {
            val alpha = alphaOfLine(top, context.transformation.scaledGridWidth)
            if (alpha == 0.0) continue

            val (x1, y1) = context.transformation.planetToCanvas(Point(rectangle.left, top.toDouble()))
            val (x2, y2) = context.transformation.planetToCanvas(Point(rectangle.right, top.toDouble()))
            val p = if (isDefaultAxesOrientation) {
                val x3 = 30.0
                val y3 = (x3 - x1) / (x2 - x1) * (y2 - y1) + y1

                if (y3 > context.height - 50) continue

                Point(x3, y3)
            } else {
                val y3 = context.canvas.height - 30.0
                val x3 = (y3 - y1) / (y2 - y1) * (x2 - x1) + x1

                if (x3 < 50) continue

                Point(x3, y3)
            }

            context.canvas.fillText(top.toString(), p, context.theme.gridTextColor.a(alpha), FONT_SIZE, alignment = ICanvas.FontAlignment.CENTER)
        }

        val startLeft = ceil(rectangle.left).toInt()
        val stopLeft = floor(rectangle.right).toInt()
        for (left in startLeft..stopLeft) {
            val alpha = alphaOfLine(left, context.transformation.scaledGridWidth)
            if (alpha == 0.0) continue

            val (x1, y1) = context.transformation.planetToCanvas(Point(left.toDouble(), rectangle.top))
            val (x2, y2) = context.transformation.planetToCanvas(Point(left.toDouble(), rectangle.bottom))
            val p = if (isDefaultAxesOrientation) {
                val y3 = context.canvas.height - 30.0
                val x3 = (y3 - y1) / (y2 - y1) * (x2 - x1) + x1
                Point(x3, y3)
            } else {
                val x3 = 30.0
                val y3 = (x3 - x1) / (x2 - x1) * (y2 - y1) + y1
                Point(x3, y3)
            }

            context.canvas.fillText(left.toString(), p, context.theme.gridTextColor.a(alpha), FONT_SIZE, alignment = ICanvas.FontAlignment.CENTER)
        }
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        return emptyList()
    }

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
