package de.robolab.client.renderer.canvas

import de.robolab.client.utils.ContextMenu
import de.robolab.common.utils.Color
import de.robolab.common.utils.Point
import de.robolab.common.utils.Rectangle

class MirrorPointCanvas(private val canvas: ICanvas, private val mirrorAxes: Double) : ICanvas by canvas {

    private fun mirrorPoint(point: Point): Point {
        return Point(
            mirrorAxes - (point.x - mirrorAxes),
            point.y
        )
    }

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
            points.map(this::mirrorPoint),
            color
        )
    }

    override fun strokePolygon(points: List<Point>, color: Color, width: Double) {
        canvas.strokePolygon(
            points.map(this::mirrorPoint),
            color,
            width
        )
    }

    override fun strokeLine(points: List<Point>, color: Color, width: Double) {
        if (points.isEmpty()) return

        canvas.strokeLine(
            points.map(this::mirrorPoint),
            color,
            width
        )
    }

    override fun dashLine(points: List<Point>, color: Color, width: Double, dashes: List<Double>, dashOffset: Double) {
        if (points.isEmpty()) return

        canvas.dashLine(
            points.map(this::mirrorPoint),
            color,
            width,
            dashes,
            dashOffset
        )
    }

    override fun fillText(
        text: String,
        position: Point,
        color: Color,
        fontSize: Double,
        alignment: ICanvas.FontAlignment,
        fontWeight: ICanvas.FontWeight
    ) {
        canvas.fillText(
            text,
            mirrorPoint(position),
            color,
            fontSize,
            alignment,
            fontWeight
        )
    }

    override fun fillArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color) {
        canvas.fillArc(
            mirrorPoint(center),
            radius,
            startAngle,
            extendAngle,
            color
        )
    }

    override fun strokeArc(
        center: Point,
        radius: Double,
        startAngle: Double,
        extendAngle: Double,
        color: Color,
        width: Double
    ) {
        canvas.strokeArc(
            mirrorPoint(center),
            radius,
            startAngle,
            extendAngle,
            color,
            width
        )
    }

    override fun openContextMenu(menu: ContextMenu) {
        canvas.openContextMenu(
            menu.copy(
                position = mirrorPoint(menu.position)
            )
        )
    }
}