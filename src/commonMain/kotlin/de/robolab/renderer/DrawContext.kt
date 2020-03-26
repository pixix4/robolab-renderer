package de.robolab.renderer

import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.platform.ICanvasListener
import de.robolab.renderer.theme.ITheme
import kotlin.math.abs

class DrawContext(
        val canvas: ICanvas,
        val transformation: Transformation,
        val theme: ITheme
) : ICanvas {

    private var alphaFactor: Double = 1.0
    private fun c(color: Color): Color {
        return if (alphaFactor > 1.0) {
            color.a(color.alpha * alphaFactor)
        } else color
    }

    fun withAlpha(alphaFactor: Double, block: () -> Unit) {
        val oldAlphaFactor = alphaFactor
        this.alphaFactor = alphaFactor
        block()
        this.alphaFactor = oldAlphaFactor
    }

    override fun setListener(listener: ICanvasListener) {
        canvas.setListener(listener)
    }

    var area: Rectangle = Rectangle.ZERO
        private set

    private fun updateArea() {
        area = Rectangle.fromEdges(
                transformation.canvasToPlanet(Point(0.0, 0.0)),
                transformation.canvasToPlanet(Point(canvas.width, 0.0)),
                transformation.canvasToPlanet(Point(0.0, canvas.height)),
                transformation.canvasToPlanet(Point(canvas.width, canvas.height))
        )
    }

    override val width: Double
        get() = canvas.width
    override val height: Double
        get() = canvas.height

    override fun clear(color: Color) {
        canvas.clear(c(color))
    }

    override fun fillRect(rectangle: Rectangle, color: Color) {
        fillPolygon(
                listOf(
                        Point(rectangle.left, rectangle.top),
                        Point(rectangle.right, rectangle.top),
                        Point(rectangle.right, rectangle.bottom),
                        Point(rectangle.left, rectangle.bottom)
                ),
                c(color)
        )
    }

    override fun strokeRect(rectangle: Rectangle, color: Color, width: Double) {
        strokePolygon(
                listOf(
                        Point(rectangle.left, rectangle.top),
                        Point(rectangle.right, rectangle.top),
                        Point(rectangle.right, rectangle.bottom),
                        Point(rectangle.left, rectangle.bottom)
                ),
                c(color),
                width
        )
    }

    override fun fillPolygon(points: List<Point>, color: Color) {
        canvas.fillPolygon(
                points.map(transformation::planetToCanvas),
                c(color)
        )
    }

    override fun strokePolygon(points: List<Point>, color: Color, width: Double) {
        canvas.strokePolygon(
                points.map(transformation::planetToCanvas),
                c(color),
                width * transformation.scaledGridWidth
        )
    }

    private fun Point.speedDist(other: Point): Double {
        return abs(left - other.left) + abs(top - other.top)
    }

    private fun prepareLine(points: List<Point>): List<Point> {
        // Dirty bug fix to solve the "mountain" error on paths
        // Remove overlapping points
        var last = transformation.planetToCanvas(points.first())
        val canvasPoints = mutableListOf(last)
        for (point in points) {
            val new = transformation.planetToCanvas(point)

            if (new.speedDist(last) >= 1) {
                canvasPoints += new
                last = new
            }
        }

        return canvasPoints
    }

    override fun strokeLine(points: List<Point>, color: Color, width: Double) {
        if (points.isEmpty()) return

        canvas.strokeLine(
                prepareLine(points), //points.map(transformation::planetToCanvas),
                c(color),
                width * transformation.scaledGridWidth
        )
    }

    override fun dashLine(points: List<Point>, color: Color, width: Double, dashes: List<Double>, dashOffset: Double) {
        if (points.isEmpty()) return

        canvas.dashLine(
                prepareLine(points), //points.map(transformation::planetToCanvas),
                c(color),
                width * transformation.scaledGridWidth,
                dashes.map { it * transformation.scaledGridWidth },
                dashOffset * transformation.scaledGridWidth
        )
    }

    override fun fillText(text: String, position: Point, color: Color, fontSize: Double) {
        canvas.fillText(
                text,
                transformation.planetToCanvas(position),
                c(color),
                fontSize * transformation.scale
        )
    }

    override fun fillArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color) {
        canvas.fillArc(
                transformation.planetToCanvas(center),
                radius * transformation.scaledGridWidth,
                startAngle + transformation.rotation,
                extendAngle,
                c(color)
        )
    }

    override fun strokeArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color, width: Double) {
        canvas.strokeArc(
                transformation.planetToCanvas(center),
                radius * transformation.scaledGridWidth,
                startAngle + transformation.rotation,
                extendAngle,
                c(color),
                width * transformation.scaledGridWidth
        )
    }

    init {
        transformation.onViewChange {
            updateArea()
        }
    }
}
