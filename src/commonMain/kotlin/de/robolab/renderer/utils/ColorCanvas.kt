package de.robolab.renderer.utils

import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.platform.ICanvasListener

class ColorCanvas(private val canvas: ICanvas, private val colorAdapter: (Color) -> Color) : ICanvas by canvas {

    override fun setListener(listener: ICanvasListener) {
        canvas.setListener(listener)
    }

    override val width: Double
        get() = canvas.width
    override val height: Double
        get() = canvas.height

    override fun clear(color: Color) {
        canvas.clear(colorAdapter(color))
    }

    override fun fillRect(rectangle: Rectangle, color: Color) {
        fillPolygon(
                rectangle.toEdgeList(),
                colorAdapter(color)
        )
    }

    override fun strokeRect(rectangle: Rectangle, color: Color, width: Double) {
        strokePolygon(
                rectangle.toEdgeList(),
                colorAdapter(color),
                width
        )
    }

    override fun fillPolygon(points: List<Point>, color: Color) {
        canvas.fillPolygon(
                points,
                colorAdapter(color)
        )
    }

    override fun strokePolygon(points: List<Point>, color: Color, width: Double) {
        canvas.strokePolygon(
                points,
                colorAdapter(color),
                width
        )
    }

    override fun strokeLine(points: List<Point>, color: Color, width: Double) {
        if (points.isEmpty()) return

        canvas.strokeLine(
                points,
                colorAdapter(color),
                width
        )
    }

    override fun dashLine(points: List<Point>, color: Color, width: Double, dashes: List<Double>, dashOffset: Double) {
        if (points.isEmpty()) return

        canvas.dashLine(
                points,
                colorAdapter(color),
                width,
                dashes,
                dashOffset
        )
    }

    override fun fillText(text: String, position: Point, color: Color, fontSize: Double, alignment: ICanvas.FontAlignment) {
        canvas.fillText(
                text,
                position,
                colorAdapter(color),
                fontSize,
                alignment
        )
    }

    override fun fillArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color) {
        canvas.fillArc(
                center,
                radius,
                startAngle,
                extendAngle,
                colorAdapter(color)
        )
    }

    override fun strokeArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color, width: Double) {
        canvas.strokeArc(
                center,
                radius,
                startAngle,
                extendAngle,
                colorAdapter(color),
                width
        )
    }
}