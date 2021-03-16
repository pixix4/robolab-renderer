package de.robolab.client.ui.adapter

import de.robolab.client.renderer.canvas.CanvasListenerManager
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.canvas.ICanvasListener
import de.robolab.client.ui.views.utils.ContextMenuView
import de.robolab.client.utils.ContextMenu
import de.robolab.client.utils.electron
import de.robolab.client.utils.noElectron
import de.robolab.common.utils.Color
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Point
import de.robolab.common.utils.Rectangle
import de.westermann.kwebview.components.Canvas
import org.w3c.dom.*
import kotlin.math.PI

open class WebCanvas(val canvas: Canvas) : ICanvas {

    constructor(dimension: Dimension, scale: Double) : this(Canvas()) {
        canvas.updateSize(dimension.width.toInt(), dimension.height.toInt(), scale)
    }

    protected val context = canvas.context

    protected val listenerManager = CanvasListenerManager()
    override fun addListener(listener: ICanvasListener) {
        listenerManager += listener
    }

    override fun removeListener(listener: ICanvasListener) {
        listenerManager -= listener
    }

    override val dimension: Dimension
        get() = Dimension(canvas.fixedWidth.toDouble(), canvas.fixedHeight.toDouble())

    override fun fillRect(rectangle: Rectangle, color: Color) {
        context.fillStyle = color.toString()

        context.fillRect(
            rectangle.left,
            rectangle.top,
            rectangle.width,
            rectangle.height
        )
    }

    override fun strokeRect(rectangle: Rectangle, color: Color, width: Double) {
        context.strokeStyle = color.toString()
        context.lineWidth = width

        context.strokeRect(
            rectangle.left,
            rectangle.top,
            rectangle.width,
            rectangle.height
        )
    }

    private fun drawPath(points: List<Point>) {
        context.beginPath()
        val first = points.firstOrNull() ?: return
        context.moveTo(first.left, first.top)

        points.asSequence().drop(1).forEach {
            context.lineTo(it.left, it.top)
        }
    }

    override fun fillPolygon(points: List<Point>, color: Color) {
        context.fillStyle = color.toString()

        drawPath(points)
        context.closePath()

        context.fill()
    }

    override fun strokePolygon(points: List<Point>, color: Color, width: Double) {
        context.strokeStyle = color.toString()
        context.lineWidth = width

        drawPath(points)
        context.closePath()

        context.stroke()
    }

    override fun strokeLine(points: List<Point>, color: Color, width: Double) {
        context.strokeStyle = color.toString()
        context.lineWidth = width

        drawPath(points)

        context.stroke()
    }

    override fun dashLine(points: List<Point>, color: Color, width: Double, dashes: List<Double>, dashOffset: Double) {
        context.strokeStyle = color.toString()
        context.lineWidth = width
        context.setLineDash(dashes.toTypedArray())
        context.lineDashOffset = dashOffset

        drawPath(points)

        context.stroke()

        context.setLineDash(arrayOf())
        context.lineDashOffset = 0.0
    }

    override fun fillText(
        text: String,
        position: Point,
        color: Color,
        fontSize: Double,
        alignment: ICanvas.FontAlignment,
        fontWeight: ICanvas.FontWeight
    ) {
        context.fillStyle = color.toString()
        context.textAlign = when (alignment) {
            ICanvas.FontAlignment.LEFT -> CanvasTextAlign.LEFT
            ICanvas.FontAlignment.CENTER -> CanvasTextAlign.CENTER
            ICanvas.FontAlignment.RIGHT -> CanvasTextAlign.RIGHT
        }
        context.textBaseline = CanvasTextBaseline.MIDDLE
        val weight = when (fontWeight) {
            ICanvas.FontWeight.NORMAL -> ""
            ICanvas.FontWeight.BOLD -> "bold "
        }
        context.font = "$weight${fontSize}px 'Roboto Mono'"

        context.fillText(text, position.left, position.top)
    }

    override fun fillArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color) {
        context.fillStyle = color.toString()

        context.beginPath()

        context.arc(
            center.left,
            center.top,
            radius,
            2.0 * PI - startAngle,
            2.0 * PI - (startAngle + extendAngle),
            anticlockwise = true
        )

        context.fill()
    }

    override fun strokeArc(
        center: Point,
        radius: Double,
        startAngle: Double,
        extendAngle: Double,
        color: Color,
        width: Double
    ) {
        context.strokeStyle = color.toString()
        context.lineWidth = width

        context.beginPath()

        context.arc(
            center.left,
            center.top,
            radius,
            2.0 * PI - startAngle,
            2.0 * PI - (startAngle + extendAngle),
            anticlockwise = true
        )

        context.stroke()
    }

    override fun openContextMenu(menu: ContextMenu) {
        val m = menu.copy(position = menu.position + Point(canvas.offsetLeftTotal, canvas.offsetTopTotal))
        electron { electron ->
            electron.menu(m)
        }
        noElectron {
            ContextMenuView.open(m)
        }
    }

    override fun startClip(rectangle: Rectangle) {
        context.save()
        val region = Path2D()
        region.rect(rectangle.left, rectangle.top, rectangle.width, rectangle.height)
        context.clip(region)
    }

    override fun endClip() {
        context.restore()
    }

    init {
        context.lineCap = CanvasLineCap.BUTT
        context.lineJoin = CanvasLineJoin.MITER

        canvas.html.tabIndex = 0

        canvas.onResize {
            listenerManager.onResize(dimension)
        }
    }
}
