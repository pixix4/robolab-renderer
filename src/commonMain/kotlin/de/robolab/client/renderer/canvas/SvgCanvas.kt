package de.robolab.client.renderer.canvas

import de.robolab.client.utils.ContextMenu
import de.robolab.common.utils.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class SvgCanvas(
    override val dimension: Dimension
) : ICanvas {

    override fun addListener(listener: ICanvasListener) {
    }

    override fun removeListener(listener: ICanvasListener) {
    }

    private val stringBuilder = mutableListOf<String>()

    private fun append(line: String) {
        stringBuilder.add(line)
    }

    fun buildFile(): String {
        val content = stringBuilder.joinToString("\n") { "    $it" }

        val width = dimension.width.toFixed(2)
        val height = dimension.height.toFixed(2)

        return buildString {
            append("""<?xml version="1.0" encoding="UTF-8"?>""")
            append('\n')
            append("""<svg version="1.1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $width $height">""".trimMargin())
            append('\n')
            append("""<style type="text/css">""")
            append(FONT_LOADER)
            append("""</style>""")
            append('\n')
            append(content)
            append('\n')
            append("""</svg>""")
            append('\n')
        }
    }

    override fun fillRect(rectangle: Rectangle, color: Color) {
        append("""<rect x="${rectangle.left}" y="${rectangle.top}" width="${rectangle.width}" height="${rectangle.height}" fill="$color" stroke="none" />""")
    }

    override fun strokeRect(rectangle: Rectangle, color: Color, width: Double) {
        append("""<rect x="${rectangle.left}" y="${rectangle.top}" width="${rectangle.width}" height="${rectangle.height}" fill="none" stroke="$color" stroke-width="$width" $STROKE_CONST />""")
    }

    override fun fillPolygon(points: List<Vector>, color: Color) {
        val p = points.joinToString(" ") { "${it.left.toFixed(2)},${it.top.toFixed(2)}" }
        append("""<polygon points="$p" fill="$color" stroke="none" />""")
    }

    override fun strokePolygon(points: List<Vector>, color: Color, width: Double) {
        val p = points.joinToString(" ") { "${it.left.toFixed(2)},${it.top.toFixed(2)}" }
        append("""<polygon points="$p" fill="none" stroke="$color" stroke-width="$width" $STROKE_CONST />""")
    }

    override fun strokeLine(points: List<Vector>, color: Color, width: Double) {
        val p = points.joinToString(" ") { "${it.left.toFixed(2)},${it.top.toFixed(2)}" }
        append("""<polyline points="$p" fill="none" stroke="$color" stroke-width="$width" $STROKE_CONST />""")
    }

    override fun dashLine(points: List<Vector>, color: Color, width: Double, dashes: List<Double>, dashOffset: Double) {
        val p = points.joinToString(" ") { "${it.left.toFixed(2)},${it.top.toFixed(2)}" }
        val d = dashes.joinToString(" ") { it.toFixed(2) }
        append(
            """<polyline points="$p" fill="none" stroke="$color" stroke-width="$width" stroke-dasharray="$d" stroke-dashoffset="${dashOffset.toFixed(
                2
            )}" $STROKE_CONST />"""
        )
    }

    override fun fillText(
        text: String,
        position: Vector,
        color: Color,
        fontSize: Double,
        alignment: ICanvas.FontAlignment,
        fontWeight: ICanvas.FontWeight
    ) {
        val anchor = when (alignment) {
            ICanvas.FontAlignment.LEFT -> "start"
            ICanvas.FontAlignment.CENTER -> "middle"
            ICanvas.FontAlignment.RIGHT -> "end"
        }
        val weight = when (fontWeight) {
            ICanvas.FontWeight.NORMAL -> "normal"
            ICanvas.FontWeight.BOLD -> "bold"
        }
        append(
            """<text x="${position.left}" y="${position.top}" fill="$color" stroke="none" font-family="Roboto Mono" font-weight="$weight" font-size="${fontSize.toFixed(
                2
            )}" text-anchor="$anchor" dominant-baseline="middle">$text</text>""".trimMargin()
        )
    }

    private fun calcArc(center: Vector, radius: Double, startAngle: Double, extendAngle: Double): String {
        val sa = 2.0 * PI - startAngle
        val ea = 2.0 * PI - (startAngle + extendAngle)
        val (x1, y1) = center + Vector(radius * cos(sa), radius * sin(sa))
        val (x2, y2) = center + Vector(radius * cos(ea), radius * sin(ea))
        val fA = if (extendAngle > PI) 1 else 0
        val fS = if (extendAngle > 0) 0 else 1
        return "M ${x1.toFixed(2)},${y1.toFixed(2)} A ${radius.toFixed(2)} ${radius.toFixed(2)} 0 $fA $fS ${x2.toFixed(2)},${y2.toFixed(
            2
        )}"
    }

    override fun fillArc(center: Vector, radius: Double, startAngle: Double, extendAngle: Double, color: Color) {
        if (extendAngle < 2 * PI) {
            val d = calcArc(center, radius, startAngle, extendAngle)
            append("""<path d="$d" fill="$color" stroke="none" />""")
        } else {
            append("""<circle cx="${center.left.toFixed(2)}" cy="${center.top.toFixed(2)}" r="${radius.toFixed(2)}" fill="$color" stroke="none" />""")
        }
    }

    override fun strokeArc(
        center: Vector,
        radius: Double,
        startAngle: Double,
        extendAngle: Double,
        color: Color,
        width: Double
    ) {
        if (extendAngle < 2 * PI) {
            val d = calcArc(center, radius, startAngle, extendAngle)
            append("""<path d="$d" fill="none" stroke="$color" stroke-width="$width" $STROKE_CONST />""")
        } else {
            append("""<circle cx="${center.left.toFixed(2)}" cy="${center.top.toFixed(2)}" r="${radius.toFixed(2)}" fill="none" stroke="$color" stroke-width="$width" $STROKE_CONST />""")
        }
    }

    override fun openContextMenu(menu: ContextMenu) {
    }

    override fun startClip(rectangle: Rectangle) {
    }

    override fun endClip() {
    }

    companion object {
        private const val STROKE_CONST = """stroke-linecap="butt" stroke-linejoin="miter""""

        private const val FONT_LOADER = """
@font-face {
  font-family: 'Roboto Mono';
  font-style: normal;
  font-weight: 400;
  font-display: swap;
  src: local('Roboto Mono'), local('RobotoMono-Regular'), url(https://fonts.gstatic.com/s/robotomono/v7/L0x5DF4xlVMF-BfR8bXMIjhLq38.woff2) format('woff2');
  unicode-range: U+0000-00FF, U+0131, U+0152-0153, U+02BB-02BC, U+02C6, U+02DA, U+02DC, U+2000-206F, U+2074, U+20AC, U+2122, U+2191, U+2193, U+2212, U+2215, U+FEFF, U+FFFD;
}
@font-face {
  font-family: 'Roboto Mono';
  font-style: normal;
  font-weight: 700;
  font-display: swap;
  src: local('Roboto Mono Bold'), local('RobotoMono-Bold'), url(https://fonts.gstatic.com/s/robotomono/v7/L0xkDF4xlVMF-BfR8bXMIjDwjmqxf78.woff2) format('woff2');
  unicode-range: U+0000-00FF, U+0131, U+0152-0153, U+02BB-02BC, U+02C6, U+02DA, U+02DC, U+2000-206F, U+2074, U+20AC, U+2122, U+2191, U+2193, U+2212, U+2215, U+FEFF, U+FFFD;
}
"""
    }
}
