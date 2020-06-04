package de.robolab.common.planet

import de.robolab.common.utils.Point

data class Comment(
    val point: Point,
    val alignment: Alignment,
    val lines: List<String>
) {

    enum class Alignment {
        LEFT, CENTER, RIGHT
    }
}
