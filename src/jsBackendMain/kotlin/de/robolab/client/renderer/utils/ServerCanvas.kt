package de.robolab.client.renderer.utils

import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.canvas.ICanvasListener
import de.robolab.client.utils.ContextMenu
import de.robolab.common.utils.Color
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Point
import de.robolab.common.utils.Rectangle
import org.w3c.dom.*
import kotlin.math.PI

class ServerCanvas(override val dimension: Dimension, private val scale: Double) : ICanvas {

    val canvas = createCanvas(
        (dimension.width * scale).toInt(),
        (dimension.height * scale).toInt()
    )
    private val context = canvas.getContext("2d")

    override fun addListener(listener: ICanvasListener) {}

    override fun removeListener(listener: ICanvasListener) {}

    override fun fillRect(rectangle: Rectangle, color: Color) {
        context.fillStyle = color.toString()

        context.fillRect(
            rectangle.left * scale,
            rectangle.top * scale,
            rectangle.width * scale,
            rectangle.height * scale
        )
    }

    override fun strokeRect(rectangle: Rectangle, color: Color, width: Double) {
        context.strokeStyle = color.toString()
        context.lineWidth = width * scale

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
        context.moveTo(first.left * scale, first.top * scale)

        points.asSequence().drop(1).forEach {
            context.lineTo(it.left * scale, it.top * scale)
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
        context.lineWidth = width * scale

        drawPath(points)
        context.closePath()

        context.stroke()
    }

    override fun strokeLine(points: List<Point>, color: Color, width: Double) {
        context.strokeStyle = color.toString()
        context.lineWidth = width * scale

        drawPath(points)

        context.stroke()
    }

    override fun dashLine(points: List<Point>, color: Color, width: Double, dashes: List<Double>, dashOffset: Double) {
        context.strokeStyle = color.toString()
        context.lineWidth = width * scale
        context.setLineDash(dashes.map { it * scale }.toTypedArray())
        context.lineDashOffset = dashOffset * scale

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
        context.font = "$weight${fontSize * scale}px 'Roboto Mono'"

        context.fillText(text, position.left * scale, position.top * scale)
    }

    override fun fillArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color) {
        context.fillStyle = color.toString()

        context.beginPath()

        context.arc(
            center.left * scale,
            center.top * scale,
            radius * scale,
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
        context.lineWidth = width * scale

        context.beginPath()

        context.arc(
            center.left * scale,
            center.top * scale,
            radius * scale,
            2.0 * PI - startAngle,
            2.0 * PI - (startAngle + extendAngle),
            anticlockwise = true
        )

        context.stroke()
    }

    override fun openContextMenu(menu: ContextMenu) {
    }

    override fun startClip(rectangle: Rectangle) {
        context.save()
        val region = Path2D()
        region.rect(rectangle.left * scale, rectangle.top * scale, rectangle.width * scale, rectangle.height * scale)
        context.clip(region)
    }

    override fun endClip() {
        context.restore()
    }

    init {
        context.lineCap = CanvasLineCap.BUTT
        context.lineJoin = CanvasLineJoin.MITER
    }

    private data class Font(
        val path: String,
        val family: String,
        val weight: String
    )

    companion object {
        private val module = js("""require("canvas")""")

        fun createCanvas(width: Int, height: Int): Canvas {
            return module.createCanvas(width, height).unsafeCast<Canvas>()
        }

        private fun registerFont(path: String, family: String? = null, weight: String? = null, style: String? = null) {
            val options = js("{}")
            if (family != null) options.family = family
            if (weight != null) options.weight = weight
            if (style != null) options.style = style
            module.registerFont(
                path,
                options
            )
        }

        private val fontList = listOf(
            Font("RobotoMono/RobotoMono-Regular.ttf", "Roboto Mono", "regular"),
            Font("RobotoMono/RobotoMono-Bold.ttf", "Roboto Mono", "bold"),
            Font("Roboto/Roboto-Regular.ttf", "Roboto", "regular"),
            Font("Roboto/Roboto-Bold.ttf", "Roboto", "bold"),
        )

        init {
            for (font in fontList) {
                registerFont(font.path, font.family, font.weight)
            }
        }
    }
}
