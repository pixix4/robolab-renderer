package de.robolab.drawable.utils

import de.robolab.model.Planet
import de.robolab.renderer.data.Color

object Utils {
    fun getSenderGrouping(planet: Planet) =
            (planet.targetList.map { it.exposure } + planet.pathList.map { it.exposure }).asSequence().distinct().withIndex().associate { (index, set) -> set to index }


    fun getColorByIndex(rot: Int): Color {
        val hue = 40.0 + rot * 137 % 360
        val saturation = 0.65 - (((rot / 4)) * 0.05) % 0.20
        val brightness = 0.9 - (((rot / 4)) * 0.05) % 0.20

        return Color.hsb(hue, saturation, brightness)
    }
}
