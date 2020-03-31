package de.robolab.renderer

object PlottingConstraints {
    const val POINT_SIZE = 0.2
    const val LINE_WIDTH = POINT_SIZE / 7.5
    const val HOVER_WIDTH = LINE_WIDTH
    const val LINE_HOVER_WIDTH = LINE_WIDTH + 2 * HOVER_WIDTH
    const val TARGET_RADIUS = 0.25
    const val CURVE_FIRST_POINT = 0.15
    const val CURVE_SECOND_POINT = 0.3

    const val PRECISION = 0.05
    const val PRECISION_FACTOR = 1.0 / PRECISION

    const val DASH_SEGMENT_LENGTH =  0.2
    const val DASH_SPACING = 0.06

    const val ARROW_LENGTH = 0.11
}
