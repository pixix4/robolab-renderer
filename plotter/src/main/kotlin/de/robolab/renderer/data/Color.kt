package de.robolab.renderer.data

import kotlin.math.floor
import kotlin.math.roundToInt

data class Color(
        val red: Int,
        val green: Int,
        val blue: Int,
        val alpha: Double = 1.0
) {
    fun r(red: Int) = copy(red = red)
    fun g(green: Int) = copy(green = green)
    fun b(blue: Int) = copy(blue = blue)
    fun a(alpha: Double) = copy(alpha = alpha)


    fun interpolate(other: Color, progress: Double) = Color(
            (red * (1 - progress) + other.red * progress).roundToInt(),
            (green * (1 - progress) + other.green * progress).roundToInt(),
            (blue * (1 - progress) + other.blue * progress).roundToInt(),
            alpha * (1 - progress) + other.alpha * progress
    )

    companion object {
        val TRANSPARENT = Color(0, 0, 0, 1.0)

        fun hsb(hue: Double, saturation: Double, brightness: Double): Color {
            val var6 = (hue % 360.0 + 360.0) % 360.0
            val var0 = var6 / 360.0
            var red = 0.0
            var green = 0.0
            var blue = 0.0
            if (saturation == 0.0) {
                blue = brightness
                green = brightness
                red = brightness
            } else {
                val var14 = (var0 - floor(var0)) * 6.0
                val var16 = var14 - floor(var14)
                val var18 = brightness * (1.0 - saturation)
                val var20 = brightness * (1.0 - saturation * var16)
                val var22 = brightness * (1.0 - saturation * (1.0 - var16))
                when (var14.toInt()) {
                    0 -> {
                        red = brightness
                        green = var22
                        blue = var18
                    }
                    1 -> {
                        red = var20
                        green = brightness
                        blue = var18
                    }
                    2 -> {
                        red = var18
                        green = brightness
                        blue = var22
                    }
                    3 -> {
                        red = var18
                        green = var20
                        blue = brightness
                    }
                    4 -> {
                        red = var22
                        green = var18
                        blue = brightness
                    }
                    5 -> {
                        red = brightness
                        green = var18
                        blue = var20
                    }
                }
            }

            return Color(
                    (red * 255).roundToInt(),
                    (green * 255).roundToInt(),
                    (blue * 255).roundToInt()
            )
        }

    }
}