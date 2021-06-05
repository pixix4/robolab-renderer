package de.robolab.client.renderer.drawable.utils

import de.robolab.client.renderer.canvas.DrawContext
import de.robolab.client.renderer.view.base.ViewColor
import de.robolab.common.utils.Color

data class SenderGrouping(
    val char: Char,
) {

    val color = getColorByIndex(char.code - 65)

    companion object {
        private fun getColorByIndex(index: Int): Color {
            if (index < 0) {
                return Color.TRANSPARENT
            }
            if (index < colorList.size) {
                return colorList[index]
            }
            val rot = index - colorList.size
            val hue = 40.0 + rot * 137 % 360
            val saturation = 0.65 - (((rot / 4)) * 0.05) % 0.20
            val brightness = 0.9 - (((rot / 4)) * 0.05) % 0.20

            return Color.hsb(hue, saturation, brightness)
        }

        private val colorList = listOf(
            Color(241, 196, 15),
            Color(46, 204, 113),
            Color(231, 76, 60),
            Color(155, 89, 182),
            Color(26, 188, 156),
            Color(230, 126, 34),
            Color(52, 152, 219),
            Color(243, 156, 18),
            Color(39, 174, 96),
            Color(192, 57, 43),
            Color(142, 68, 173),
            Color(22, 160, 133),
            Color(211, 84, 0),
            Color(41, 128, 185)
        )
    }
}


fun log2(a: Int): Int {
    var x = a
    var pow = 0
    if (x >= 1 shl 16) {
        x = x shr 16
        pow += 16
    }
    if (x >= 1 shl 8) {
        x = x shr 8
        pow += 8
    }
    if (x >= 1 shl 4) {
        x = x shr 4
        pow += 4
    }
    if (x >= 1 shl 2) {
        x = x shr 2
        pow += 2
    }
    if (x >= 1 shl 1) {
        //x = x shr 1
        pow += 1
    }
    return pow
}

fun power2(exp: Int): Int {
    var x = 2
    var y = exp
    var result = 1
    while (y > 0) {
        if (y and 1 == 0) {
            x *= x
            y = y ushr 1
        } else {
            result *= x
            y--
        }
    }
    return result
}

fun DrawContext.c(color: ViewColor) = color.toColor(theme.plotter)
