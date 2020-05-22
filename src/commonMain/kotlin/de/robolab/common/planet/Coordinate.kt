package de.robolab.common.planet

data class Coordinate(val x: Int, val y: Int) {

    fun getColor(bluePoint: Coordinate?): Color {
        if (bluePoint == null) {
            return Color.UNKNOWN
        }

        if ((this.x + this.y) % 2 == (bluePoint.x + bluePoint.y) % 2) {
            return Color.BLUE
        }
        return Color.RED
    }

    enum class Color {
        RED, BLUE, UNKNOWN
    }
}