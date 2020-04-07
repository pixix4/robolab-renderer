package de.robolab.renderer.drawable.utils

import de.robolab.model.Coordinate
import de.robolab.model.Direction
import de.robolab.model.Planet
import de.robolab.model.TargetPoint
import de.robolab.renderer.PlottingConstraints
import de.robolab.renderer.data.Color
import de.robolab.renderer.data.Point

object Utils {
    fun getSenderGrouping(planet: Planet): Map<Set<Coordinate>, Int> {
        return (planet.targetList.map { getTargetExposure(it, planet) } + planet.pathList.map { it.exposure })
                .filterNot { it.isEmpty() }
                .asSequence()
                .distinct()
                .withIndex()
                .associate { (index, set) -> set to index }
    }

    fun getTargetExposure(target: TargetPoint, planet: Planet): Set<Coordinate> {
        return planet.targetList.filter { it.target == target.target }.map { it.exposure }.toSet()
    }

    fun getColorByIndex(index: Int): Color {
        if (index < colorList.size) {
            return colorList[index]
        }
        val rot = index - colorList.size
        val hue = 40.0 + rot * 137 % 360
        val saturation = 0.65 - (((rot / 4)) * 0.05) % 0.20
        val brightness = 0.9 - (((rot / 4)) * 0.05) % 0.20

        return Color.hsb(hue, saturation, brightness)
    }

    fun calculateProjection(pointer: Point, referencePoint: Point, direction: Direction): Point {
        val basisVector = direction.toVector()
        val referenceVector = pointer - referencePoint

        val distance = referenceVector.dotProduct(basisVector)
        val targetVector = basisVector * distance

        return if (distance > PlottingConstraints.CURVE_FIRST_POINT) {
            referencePoint + targetVector
        } else {
            referencePoint + basisVector * PlottingConstraints.CURVE_FIRST_POINT
        }
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
