package de.robolab.renderer.drawable

import de.robolab.renderer.utils.DrawContext
import de.robolab.renderer.data.Point
import de.robolab.renderer.drawable.base.IDrawable
import kotlin.math.ceil
import kotlin.math.floor

class GridLinesDrawable(private val planetDrawable: PlanetDrawable) : IDrawable {

    override fun onUpdate(ms_offset: Double): Boolean {
        return false
    }

    override fun onDraw(context: DrawContext) {
        if (!planetDrawable.drawGridLines) return

        val rectangle = context.area

        val startTop = ceil(rectangle.top).toInt()
        val stopTop = floor(rectangle.bottom).toInt()
        for (top in startTop..stopTop) {
            val alpha = GridNumbersDrawable.alphaOfLine(top, context.transformation.scaledGridWidth)
            if (alpha == 0.0) continue

            val p1 = context.transformation.planetToCanvas(Point(rectangle.left, top.toDouble()))
            val p2 = context.transformation.planetToCanvas(Point(rectangle.right, top.toDouble()))

            context.canvas.strokeLine(
                    listOf(
                            p1,
                            p2
                    ),
                    context.theme.gridColor.a(alpha),
                    1.0
            )
        }

        val startLeft = ceil(rectangle.left).toInt()
        val stopLeft = floor(rectangle.right).toInt()
        for (left in startLeft..stopLeft) {
            val alpha = GridNumbersDrawable.alphaOfLine(left, context.transformation.scaledGridWidth)
            if (alpha == 0.0) continue

            val p1 = context.transformation.planetToCanvas(Point(left.toDouble(), rectangle.top))
            val p2 = context.transformation.planetToCanvas(Point(left.toDouble(), rectangle.bottom))

            context.canvas.strokeLine(
                    listOf(
                            p1,
                            p2
                    ),
                    context.theme.gridColor.a(alpha),
                    1.0
            )
        }
    }

    override fun getObjectsAtPosition(context: DrawContext, position: Point): List<Any> {
        return emptyList()
    }
}
