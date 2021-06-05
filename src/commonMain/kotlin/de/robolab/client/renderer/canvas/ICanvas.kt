package de.robolab.client.renderer.canvas

import de.robolab.client.utils.ContextMenu
import de.robolab.common.utils.Color
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Vector
import de.robolab.common.utils.Rectangle

interface ICanvas {

    fun addListener(listener: ICanvasListener)

    fun removeListener(listener: ICanvasListener)

    val dimension: Dimension

    fun fillRect(rectangle: Rectangle, color: Color)

    fun strokeRect(rectangle: Rectangle, color: Color, width: Double = 1.0)

    fun fillPolygon(points: List<Vector>, color: Color)

    fun strokePolygon(points: List<Vector>, color: Color, width: Double = 1.0)

    fun strokeLine(points: List<Vector>, color: Color, width: Double = 1.0)

    fun dashLine(points: List<Vector>, color: Color, width: Double = 1.0, dashes: List<Double> = emptyList(), dashOffset: Double = 0.0)

    fun fillText(text: String, position: Vector, color: Color, fontSize: Double = 12.0, alignment: FontAlignment = FontAlignment.LEFT, fontWeight: FontWeight = FontWeight.NORMAL)

    fun fillArc(center: Vector, radius: Double, startAngle: Double, extendAngle: Double, color: Color)

    fun strokeArc(center: Vector, radius: Double, startAngle: Double, extendAngle: Double, color: Color, width: Double = 1.0)

    fun openContextMenu(menu: ContextMenu)

    fun startClip(rectangle: Rectangle)
    fun endClip()

    enum class FontAlignment {
        LEFT, CENTER, RIGHT
    }

    enum class FontWeight {
        NORMAL, BOLD
    }
}
