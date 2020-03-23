package de.robolab.renderer.drawable

import de.robolab.renderer.DrawContext
import de.robolab.renderer.data.Point
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor

object GridNumbersDrawable : IDrawable {

    override fun onUpdate(ms_offset: Double): Boolean {
        return false
    }

    override fun onDraw(context: DrawContext) {
        val fontSize = 16.0

        val rectangle = context.area
        val lineModulo = ceil((fontSize * 1.5) / context.transformation.scaledGridWidth).toInt()
        val isDefaultAxesOrientation = cos(context.transformation.rotation * 2) >= 0.0

        val startTop = ceil(rectangle.top).toInt()
        val stopTop = floor(rectangle.bottom).toInt()
        for (top in startTop..stopTop) {
            if (top % lineModulo != 0) continue

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

            context.canvas.fillText(top.toString(), p, context.theme.gridTextColor, fontSize)
        }

        val startLeft = ceil(rectangle.left).toInt()
        val stopLeft = floor(rectangle.right).toInt()
        for (left in startLeft..stopLeft) {
            if (left % lineModulo != 0) continue

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

            context.canvas.fillText(left.toString(), p, context.theme.gridTextColor, fontSize)
        }
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        return emptyList()
    }
}
