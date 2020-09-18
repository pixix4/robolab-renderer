package de.robolab.client.ui.adapter

import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.canvas.ICanvasListener
import de.robolab.client.ui.style.MainStyle
import de.robolab.client.utils.ContextMenu
import de.robolab.common.utils.Color
import de.robolab.common.utils.Dimension
import de.robolab.common.utils.Point
import de.robolab.common.utils.Rectangle
import java.awt.BasicStroke
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.RenderingHints
import java.awt.geom.Path2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.io.File
import java.lang.invoke.MethodHandles
import javax.imageio.ImageIO
import kotlin.math.PI
import kotlin.math.roundToInt


class AwtCanvas(
    override val dimension: Dimension,
    scale: Double
) : ICanvas {

    private val bufferedImage =
        BufferedImage((dimension.width * scale).toInt(), (dimension.height * scale).toInt(), BufferedImage.TYPE_INT_ARGB)
    private val context = bufferedImage.createGraphics()

    fun writePNG(file: File) {
        ImageIO.write(
            bufferedImage,
            "PNG",
            file
        )
    }

    override fun addListener(listener: ICanvasListener) {
    }

    override fun removeListener(listener: ICanvasListener) {
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
        val font = Font("Roboto Mono", mask, fontSize.roundToInt())
        context.font = font
        val metrics = context.getFontMetrics(font)

        var x = position.left.toFloat()
        var y = position.top.toFloat()

        when (alignment) {
            ICanvas.FontAlignment.LEFT -> {
            }
            ICanvas.FontAlignment.CENTER -> {
                val width = metrics.stringWidth(text) + 1.0
                x -= (width / 2.0).toFloat()
            }
            ICanvas.FontAlignment.RIGHT -> {
                val width = metrics.stringWidth(text) + 1.0f
                x -= width
            }
        }

        y += (metrics.height.toFloat() / 3.8).toFloat()

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

    override fun startClip(rectangle: Rectangle) {
    }

    override fun endClip() {
    }

    init {
        importFonts()

        context.addRenderingHints(
            mutableMapOf(
                RenderingHints.KEY_ANTIALIASING to RenderingHints.VALUE_ANTIALIAS_ON,
                RenderingHints.KEY_DITHERING to RenderingHints.VALUE_DITHER_ENABLE,
                RenderingHints.KEY_FRACTIONALMETRICS to RenderingHints.VALUE_FRACTIONALMETRICS_ON,
                RenderingHints.KEY_INTERPOLATION to RenderingHints.VALUE_INTERPOLATION_BICUBIC,
                RenderingHints.KEY_RENDERING to RenderingHints.VALUE_RENDER_QUALITY,
                RenderingHints.KEY_COLOR_RENDERING to RenderingHints.VALUE_COLOR_RENDER_QUALITY,
                RenderingHints.KEY_TEXT_ANTIALIASING to RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
            )
        )
        context.scale(scale, scale)
    }

    companion object {
        private fun importFont(path: String) {
            val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
            val stream = MethodHandles.lookup().lookupClass().getResourceAsStream(path) ?: return
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, stream))
        }

        private var isImported = false
        fun importFonts() {
            if (!isImported) {
                isImported = true

                for (font in MainStyle.monospaceFonts) {
                    importFont(font)
                }
            }
        }
    }
}

fun Color.toPaint(): java.awt.Color {
    return java.awt.Color(red, green, blue, (alpha * 255).toInt())
}