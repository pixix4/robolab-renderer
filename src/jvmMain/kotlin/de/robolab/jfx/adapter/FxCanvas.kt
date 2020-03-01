package de.robolab.jfx.adapter

import de.robolab.jfx.ResizeableCanvas
import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Dimension
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.platform.*
import javafx.geometry.VPos
import javafx.scene.shape.ArcType
import javafx.scene.shape.StrokeLineCap
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import kotlin.math.PI

class FxCanvas : ICanvas {

    val canvas = ResizeableCanvas()
    private val context = canvas.graphicsContext2D

    override fun setListener(listener: ICanvasListener) {
        canvas.setOnMousePressed { event ->
            listener.onMouseDown(MouseEvent(
                    Point(event.x, event.y),
                    Dimension(width, height),
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
            ))
        }
        canvas.setOnMouseReleased { event ->
            listener.onMouseUp(MouseEvent(
                    Point(event.x, event.y),
                    Dimension(width, height),
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
            ))
        }
        canvas.setOnMouseDragged { event ->
            listener.onMouseDrag(MouseEvent(
                    Point(event.x, event.y),
                    Dimension(width, height),
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
            ))
        }
        canvas.setOnMouseMoved { event ->
            listener.onMouseMove(MouseEvent(
                    Point(event.x, event.y),
                    Dimension(width, height),
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
            ))
        }
        canvas.setOnMouseClicked { event ->
            listener.onMouseClick(MouseEvent(
                    Point(event.x, event.y),
                    Dimension(width, height),
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
            ))
        }
        canvas.setOnScroll { event ->
            listener.onScroll(ScrollEvent(
                    Point(event.x, event.y),
                    Point(event.deltaX, event.deltaY),
                    Dimension(width, height),
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
            ))
        }
        canvas.setOnZoom { event ->
            listener.onZoom(ZoomEvent(
                    Point(event.x, event.y),
                    event.zoomFactor,
                    Dimension(width, height),
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
            ))
        }
        canvas.setOnRotate { event ->
            listener.onRotate(RotateEvent(
                    Point(event.x, event.y),
                    event.angle / 180.0 * PI,
                    Dimension(width, height),
                    event.isControlDown,
                    event.isAltDown,
                    event.isShiftDown
            ))
        }
        canvas.addDrawHook {
            listener.onResize(Dimension(width, height))
        }
    }

    override val width: Double
        get() = canvas.width

    override val height: Double
        get() = canvas.height

    override fun clear(color: Color) {
        fillRect(Rectangle(
                0.0,
                0.0,
                width,
                height
        ), color)
    }

    override fun fillRect(rectangle: Rectangle, color: Color) {
        context.fill = color.fx()

        context.fillRect(
                rectangle.left,
                rectangle.top,
                rectangle.width,
                rectangle.height
        )
    }

    override fun strokeRect(rectangle: Rectangle, color: Color, width: Double) {
        context.stroke = color.fx()
        context.lineWidth = width

        context.strokeRect(
                rectangle.left,
                rectangle.top,
                rectangle.width,
                rectangle.height
        )
    }

    override fun fillPolygon(points: List<Point>, color: Color) {
        context.fill = color.fx()

        context.fillPolygon(
                points.map { it.left }.toDoubleArray(),
                points.map { it.top }.toDoubleArray(),
                points.size
        )
    }

    override fun strokePolygon(points: List<Point>, color: Color, width: Double) {
        context.stroke = color.fx()
        context.lineWidth = width

        context.strokePolygon(
                points.map { it.left }.toDoubleArray(),
                points.map { it.top }.toDoubleArray(),
                points.size
        )
    }

    override fun strokeLine(points: List<Point>, color: Color, width: Double) {
        context.stroke = color.fx()
        context.lineWidth = width

        context.beginPath()
        val first = points.firstOrNull() ?: return
        context.moveTo(first.left, first.top)

        points.asSequence().drop(1).forEach {
            context.lineTo(it.left, it.top)
        }

        context.stroke()
    }

    override fun fillText(text: String, position: Point, color: Color, fontSize: Double) {
        context.fill = color.fx()
        context.textAlign = TextAlignment.CENTER
        context.textBaseline = VPos.CENTER
        context.font = Font.font(fontSize)

        context.fillText(text, position.left, position.top)
    }

    override fun fillArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color) {
        context.fill = color.fx()

        context.fillArc(
                center.left - radius,
                center.top - radius,
                radius * 2,
                radius * 2,
                startAngle / PI * 180.0,
                extendAngle / PI * 180.0,
                ArcType.CHORD
        )
    }

    override fun strokeArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color, width: Double) {
        context.stroke = color.fx()
        context.lineWidth = width

        context.strokeArc(
                center.left - radius,
                center.top - radius,
                radius * 2,
                radius * 2,
                startAngle / PI * 180.0,
                extendAngle / PI * 180.0,
                ArcType.OPEN
        )
    }

    init {
        context.lineCap = StrokeLineCap.BUTT
    }
}

fun Color.fx(): javafx.scene.paint.Color = javafx.scene.paint.Color.rgb(red, green, blue, alpha)
