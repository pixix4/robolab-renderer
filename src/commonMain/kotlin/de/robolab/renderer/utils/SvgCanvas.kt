package de.robolab.renderer.utils

import de.robolab.app.model.file.toFixed
import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Point
import de.robolab.renderer.data.Rectangle
import de.robolab.renderer.platform.ICanvas
import de.robolab.renderer.platform.ICanvasListener
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class SvgCanvas(
        override val width: Double,
        override val height: Double
) : ICanvas {
    override fun setListener(listener: ICanvasListener) {
    }

    private val stringBuilder = mutableListOf<String>()

    private fun append(line: String) {
        stringBuilder.add(line)
    }

    fun buildFile(): String {
        val content = stringBuilder.joinToString("\n") { "    $it" }

        return buildString {
            append("""<?xml version="1.0" encoding="UTF-8"?>""")
            append('\n')
            append("""<svg version="1.1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${width.toFixed(2)} ${height.toFixed(2)}">""")
            append('\n')
            append(content)
            append('\n')
            append("""</svg>""")
            append('\n')
        }
    }

    override fun clear(color: Color) {
        stringBuilder.clear()
        append("""<rect width="100%" height="100%" fill="$color" />""")
    }

    override fun fillRect(rectangle: Rectangle, color: Color) {
        append("""<rect x="${rectangle.left}" y="${rectangle.top}" width="${rectangle.width}" height="${rectangle.height}" fill="$color" stroke="none" />""")
    }

    override fun strokeRect(rectangle: Rectangle, color: Color, width: Double) {
        append("""<rect x="${rectangle.left}" y="${rectangle.top}" width="${rectangle.width}" height="${rectangle.height}" fill="none" stroke="$color" stroke-width="$width" $STROKE_CONST />""")
    }

    override fun fillPolygon(points: List<Point>, color: Color) {
        val p = points.joinToString(" ") { "${it.left.toFixed(2)},${it.top.toFixed(2)}" }
        append("""<polygon points="$p" fill="$color" stroke="none" />""")
    }

    override fun strokePolygon(points: List<Point>, color: Color, width: Double) {
        val p = points.joinToString(" ") { "${it.left.toFixed(2)},${it.top.toFixed(2)}" }
        append("""<polygon points="$p" fill="none" stroke="$color" stroke-width="$width" $STROKE_CONST />""")
    }

    override fun strokeLine(points: List<Point>, color: Color, width: Double) {
        val p = points.joinToString(" ") { "${it.left.toFixed(2)},${it.top.toFixed(2)}" }
        append("""<polyline points="$p" fill="none" stroke="$color" stroke-width="$width" $STROKE_CONST />""")
    }

    override fun dashLine(points: List<Point>, color: Color, width: Double, dashes: List<Double>, dashOffset: Double) {
        val p = points.joinToString(" ") { "${it.left.toFixed(2)},${it.top.toFixed(2)}" }
        val d = dashes.joinToString(" ") { it.toFixed(2) }
        append("""<polyline points="$p" fill="none" stroke="$color" stroke-width="$width" stroke-dasharray="$d" stroke-dashoffset="${dashOffset.toFixed(2)}" $STROKE_CONST />""")
    }

    override fun fillText(text: String, position: Point, color: Color, fontSize: Double, alignment: ICanvas.FontAlignment, fontWeight: ICanvas.FontWeight) {
        val anchor = when (alignment) {
            ICanvas.FontAlignment.LEFT -> "start"
            ICanvas.FontAlignment.CENTER -> "middle"
            ICanvas.FontAlignment.RIGHT -> "end"
        }
        val weight = when (fontWeight) {
            ICanvas.FontWeight.NORMAL -> "normal"
            ICanvas.FontWeight.BOLD -> "bold"
        }
        append("""<text x="${position.left}" y="${position.top}" fill="$color" stroke="none" font-family="sans-serif" font-weight="$weight" font-size="${fontSize.toFixed(2)}" text-anchor="$anchor" dominant-baseline="middle">$text</text>""".trimMargin())
    }

    private fun calcArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double): String {
        val sa = 2.0 * PI - startAngle
        val ea = 2.0 * PI - (startAngle + extendAngle)
        val (x1, y1) = center + Point(radius * cos(sa), radius * sin(sa))
        val (x2, y2) = center + Point(radius * cos(ea), radius * sin(ea))
        val fA = if (extendAngle > PI) 1 else 0
        val fS = if (extendAngle > 0) 0 else 1
        return "M ${x1.toFixed(2)},${y1.toFixed(2)} A ${radius.toFixed(2)} ${radius.toFixed(2)} 0 $fA $fS ${x2.toFixed(2)},${y2.toFixed(2)}"
    }

    override fun fillArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color) {
        if (extendAngle < 2 * PI) {
            val d = calcArc(center, radius, startAngle, extendAngle)
            append("""<path d="$d" fill="$color" stroke="none" />""")
        } else {
            append("""<circle cx="${center.left.toFixed(2)}" cy="${center.top.toFixed(2)}" r="${radius.toFixed(2)}" fill="$color" stroke="none" />""")
        }
    }

    override fun strokeArc(center: Point, radius: Double, startAngle: Double, extendAngle: Double, color: Color, width: Double) {
        if (extendAngle < 2 * PI) {
            val d = calcArc(center, radius, startAngle, extendAngle)
            append("""<path d="$d" fill="none" stroke="$color" stroke-width="$width" $STROKE_CONST />""")
        } else {
            append("""<circle cx="${center.left.toFixed(2)}" cy="${center.top.toFixed(2)}" r="${radius.toFixed(2)}" fill="none" stroke="$color" stroke-width="$width" $STROKE_CONST />""")
        }
    }

    companion object {
        private const val STROKE_CONST = """stroke-linecap="butt" stroke-linejoin="miter""""
    }
}
