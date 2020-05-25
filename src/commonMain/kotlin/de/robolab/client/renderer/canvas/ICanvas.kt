package de.robolab.client.renderer.canvas

import de.robolab.client.utils.ContextMenu
import de.robolab.common.utils.Color
import de.robolab.common.utils.Point
import de.robolab.common.utils.Rectangle

interface ICanvas {

    fun setListener(listener: ICanvasListener)

    val width: Double

    val height: Double

    fun clear(color: Color)

    fun fillRect(rectangle: Rectangle, color: Color)

    fun strokeRect(rectangle: Rectangle, color: Color, width: Double = 1.0)

    fun fillPolygon(points: List<Point>, color: Color)

    fun strokePolygon(points: List<Point>, color: Color, width: Double = 1.0)

    fun strokeLine(points: List<Point>, color: Color, width: Double = 1.0)

    fun dashLine(points: List<Point>, color: Color, width: Double = 1.0, dashes: List<Double> = emptyList(), dashOffset: Double = 0.0)

    fun fillText(text: String, position: Point, color: Color, fontSize: Double = 12.0, alignment: FontAlignment = FontAlignment.LEFT, fontWeight: FontWeight = FontWeight.NORMAL)

    fun fillArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color)

    fun strokeArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color, width: Double = 1.0)

    fun openContextMenu(menu: ContextMenu)

    enum class FontAlignment {
        LEFT, CENTER, RIGHT
    }

    enum class FontWeight {
        NORMAL, BOLD
    }
}