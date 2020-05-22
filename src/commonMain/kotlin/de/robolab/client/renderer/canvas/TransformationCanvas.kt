package de.robolab.client.renderer.canvas

import de.robolab.client.renderer.utils.ITransformation
import de.robolab.client.utils.ContextMenu
import de.robolab.common.utils.Color
import de.robolab.common.utils.Point
import de.robolab.common.utils.Rectangle

class TransformationCanvas(private val canvas: ICanvas, private val transformation: ITransformation) : ICanvas by canvas {

    override fun fillRect(rectangle: Rectangle, color: Color) {
        fillPolygon(
                rectangle.toEdgeList(),
                color
        )
    }

    override fun strokeRect(rectangle: Rectangle, color: Color, width: Double) {
        strokePolygon(
                rectangle.toEdgeList(),
                color,
                width
        )
    }

    override fun fillPolygon(points: List<Point>, color: Color) {
        canvas.fillPolygon(
                points.map(transformation::planetToCanvas),
                color
        )
    }

    override fun strokePolygon(points: List<Point>, color: Color, width: Double) {
        canvas.strokePolygon(
                points.map(transformation::planetToCanvas),
                color,
                width * transformation.scaledGridWidth
        )
    }

    private fun prepareLine(points: List<Point>): List<Point> {
        // Dirty bug fix to solve the "mountain" error on paths
        // Remove overlapping points
        var last = transformation.planetToCanvas(points.first())
        val canvasPoints = mutableListOf(last)
        for (point in points) {
            val new = transformation.planetToCanvas(point)

            if (new.manhattanDistanceTo(last) >= 1) {
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
                color,
                width * transformation.scaledGridWidth
        )
    }

    override fun dashLine(points: List<Point>, color: Color, width: Double, dashes: List<Double>, dashOffset: Double) {
        if (points.isEmpty()) return

        canvas.dashLine(
                prepareLine(points), //points.map(transformation::planetToCanvas),
                color,
                width * transformation.scaledGridWidth,
                dashes.map { it * transformation.scaledGridWidth },
                dashOffset * transformation.scaledGridWidth
        )
    }

    override fun fillText(text: String, position: Point, color: Color, fontSize: Double, alignment: ICanvas.FontAlignment, fontWeight: ICanvas.FontWeight) {
        canvas.fillText(
                text,
                transformation.planetToCanvas(position),
                color,
                fontSize * transformation.scale,
                alignment,
                fontWeight
        )
    }

    override fun fillArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color) {
        canvas.fillArc(
                transformation.planetToCanvas(center),
                radius * transformation.scaledGridWidth,
                startAngle + transformation.rotation,
                extendAngle,
                color
        )
    }

    override fun strokeArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color, width: Double) {
        canvas.strokeArc(
                transformation.planetToCanvas(center),
                radius * transformation.scaledGridWidth,
                startAngle + transformation.rotation,
                extendAngle,
                color,
                width * transformation.scaledGridWidth
        )
    }

    override fun openContextMenu(menu: ContextMenu) {
        canvas.openContextMenu(menu.copy(
                position = transformation.planetToCanvas(menu.position)
        ))
    }
}