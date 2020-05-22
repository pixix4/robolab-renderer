package de.robolab.client.jfx.adapter

import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.canvas.ICanvasListener
import de.robolab.client.utils.ContextMenu
import de.robolab.common.utils.Color
import de.robolab.common.utils.Point
import de.robolab.common.utils.Rectangle
import java.awt.BasicStroke
import java.awt.Font
import java.awt.RenderingHints
import java.awt.geom.Path2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.PI

class AwtCanvas(
    override val width: Double,
    override val height: Double,
    private val scale: Double
) : ICanvas {

    private val bufferedImage =
        BufferedImage((width * scale).toInt(), (height * scale).toInt(), BufferedImage.TYPE_INT_ARGB)
    private val context = bufferedImage.createGraphics()

    fun writePNG(file: File) {
        ImageIO.write(
            bufferedImage,
            "PNG",
            file
        )
    }


    override fun setListener(listener: ICanvasListener) {
    }

    override fun clear(color: Color) {
        fillRect(
            Rectangle(
                0.0,
                0.0,
                width,
                height
            ), color
        )
    }

    override fun fillRect(rectangle: Rectangle, color: Color) {
        context.paint = color.toPaint()

        val shape = Rectangle2D.Double(
            rectangle.left,
            rectangle.top,
            rectangle.width,
            rectangle.height
        )
        context.fill(shape)
    }

    override fun strokeRect(rectangle: Rectangle, color: Color, width: Double) {
        context.paint = color.toPaint()
        context.stroke = BasicStroke(width.toFloat(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)

        val shape = Rectangle2D.Double(
            rectangle.left,
            rectangle.top,
            rectangle.width,
            rectangle.height
        )
        context.draw(shape)
    }

    private fun drawPath(points: List<Point>): Path2D.Double {
        val shape = Path2D.Double()
        val first = points.firstOrNull() ?: return shape
        shape.moveTo(first.left, first.top)

        points.asSequence().drop(1).forEach {
            shape.lineTo(it.left, it.top)
        }

        return shape
    }

    override fun fillPolygon(points: List<Point>, color: Color) {
        context.paint = color.toPaint()

        val shape = drawPath(points)
        shape.closePath()

        context.fill(shape)
    }

    override fun strokePolygon(points: List<Point>, color: Color, width: Double) {
        context.paint = color.toPaint()
        context.stroke = BasicStroke(width.toFloat(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)

        val shape = drawPath(points)
        shape.closePath()

        context.draw(shape)
    }

    override fun strokeLine(points: List<Point>, color: Color, width: Double) {
        context.paint = color.toPaint()
        context.stroke = BasicStroke(width.toFloat(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)

        val shape = drawPath(points)
        context.draw(shape)
    }

    override fun dashLine(points: List<Point>, color: Color, width: Double, dashes: List<Double>, dashOffset: Double) {
        context.paint = color.toPaint()
        context.stroke = BasicStroke(
            width.toFloat(),
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10.0f,
            dashes.map { it.toFloat() }.toFloatArray(),
            dashOffset.toFloat()
        )

        val shape = drawPath(points)
        context.draw(shape)
    }

    override fun fillText(
        text: String,
        position: Point,
        color: Color,
        fontSize: Double,
        alignment: ICanvas.FontAlignment,
        fontWeight: ICanvas.FontWeight
    ) {
        context.paint = color.toPaint()

        val mask = when (fontWeight) {
            ICanvas.FontWeight.NORMAL -> Font.PLAIN
            ICanvas.FontWeight.BOLD -> Font.BOLD
        }
        context.font = Font(Font.SANS_SERIF, mask, fontSize.toInt())

        val metrics = context.fontMetrics

        var x = position.left.toFloat()
        var y = position.top.toFloat()

        when (alignment) {
            ICanvas.FontAlignment.LEFT -> {
            }
            ICanvas.FontAlignment.CENTER -> {
                val width = metrics.stringWidth(text)
                x -= width / 2
            }
            ICanvas.FontAlignment.RIGHT -> {
                val width = metrics.stringWidth(text)
                x -= width
            }
        }

        y += metrics.height / 3

        context.drawString(text, x, y)
    }

    override fun fillArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color) {
        context.paint = color.toPaint()

        context.fillArc(
            (center.left - radius).toInt(),
            (center.top - radius).toInt(),
            (radius * 2).toInt(),
            (radius * 2).toInt(),
            (startAngle / PI * 180.0).toInt(),
            (extendAngle / PI * 180.0).toInt()
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
        context.paint = color.toPaint()
        context.stroke = BasicStroke(width.toFloat(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)

        context.drawArc(
            (center.left - radius).toInt(),
            (center.top - radius).toInt(),
            (radius * 2).toInt(),
            (radius * 2).toInt(),
            (startAngle / PI * 180.0).toInt(),
            (extendAngle / PI * 180.0).toInt()
        )
    }

    override fun openContextMenu(menu: ContextMenu) {

    }

    init {
        context.addRenderingHints(
            mutableMapOf(
                RenderingHints.KEY_ANTIALIASING to RenderingHints.VALUE_ANTIALIAS_ON
            )
        )
        context.scale(scale, scale)
    }
}

fun Color.toPaint(): java.awt.Color {
    return java.awt.Color(red, green, blue, (alpha * 255).toInt())
}
