package de.robolab.client.renderer.drawable.utils

import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.common.utils.Color
import de.robolab.common.utils.Point

object SegmentDrawer {
    private const val SEGMENT_GAP = 0.02
    private const val SEGMENT_WIDTH = 0.15
    private const val SEGMENT_LEFT = 0.2
    private const val SEGMENT_RIGHT = 0.8
    private const val SEGMENT_TOP = 1.0
    private const val SEGMENT_MIDDLE = 0.5
    private const val SEGMENT_BOTTOM = 0.0

    private val SEGMENT_A = listOf(
        Point(SEGMENT_LEFT + SEGMENT_GAP, SEGMENT_TOP),
        Point(SEGMENT_RIGHT - SEGMENT_GAP, SEGMENT_TOP),
        Point(SEGMENT_RIGHT - SEGMENT_WIDTH - SEGMENT_GAP, SEGMENT_TOP - SEGMENT_WIDTH),
        Point(SEGMENT_LEFT + SEGMENT_WIDTH + SEGMENT_GAP, SEGMENT_TOP - SEGMENT_WIDTH)
    )
    private val SEGMENT_B = listOf(
        Point(SEGMENT_RIGHT, SEGMENT_TOP - SEGMENT_GAP),
        Point(SEGMENT_RIGHT, SEGMENT_MIDDLE + SEGMENT_GAP),
        Point(SEGMENT_RIGHT - SEGMENT_WIDTH, SEGMENT_MIDDLE + SEGMENT_WIDTH / 2 + SEGMENT_GAP),
        Point(SEGMENT_RIGHT - SEGMENT_WIDTH, SEGMENT_TOP - SEGMENT_WIDTH - SEGMENT_GAP)
    )
    private val SEGMENT_C = listOf(
        Point(SEGMENT_RIGHT, SEGMENT_MIDDLE - SEGMENT_GAP),
        Point(SEGMENT_RIGHT, SEGMENT_BOTTOM + SEGMENT_GAP),
        Point(SEGMENT_RIGHT - SEGMENT_WIDTH, SEGMENT_BOTTOM + SEGMENT_WIDTH + SEGMENT_GAP),
        Point(SEGMENT_RIGHT - SEGMENT_WIDTH, SEGMENT_MIDDLE - SEGMENT_WIDTH / 2 - SEGMENT_GAP)
    )
    private val SEGMENT_D = listOf(
        Point(SEGMENT_LEFT + SEGMENT_GAP, SEGMENT_BOTTOM),
        Point(SEGMENT_RIGHT - SEGMENT_GAP, SEGMENT_BOTTOM),
        Point(SEGMENT_RIGHT - SEGMENT_WIDTH - SEGMENT_GAP, SEGMENT_BOTTOM + SEGMENT_WIDTH),
        Point(SEGMENT_LEFT + SEGMENT_WIDTH + SEGMENT_GAP, SEGMENT_BOTTOM + SEGMENT_WIDTH)
    )
    private val SEGMENT_E = listOf(
        Point(SEGMENT_LEFT, SEGMENT_MIDDLE - SEGMENT_GAP),
        Point(SEGMENT_LEFT, SEGMENT_BOTTOM + SEGMENT_GAP),
        Point(SEGMENT_LEFT + SEGMENT_WIDTH, SEGMENT_BOTTOM + SEGMENT_WIDTH + SEGMENT_GAP),
        Point(SEGMENT_LEFT + SEGMENT_WIDTH, SEGMENT_MIDDLE - SEGMENT_WIDTH / 2 - SEGMENT_GAP)
    )
    private val SEGMENT_F = listOf(
        Point(SEGMENT_LEFT, SEGMENT_TOP - SEGMENT_GAP),
        Point(SEGMENT_LEFT, SEGMENT_MIDDLE + SEGMENT_GAP),
        Point(SEGMENT_LEFT + SEGMENT_WIDTH, SEGMENT_MIDDLE + SEGMENT_WIDTH / 2 + SEGMENT_GAP),
        Point(SEGMENT_LEFT + SEGMENT_WIDTH, SEGMENT_TOP - SEGMENT_WIDTH - SEGMENT_GAP)
    )
    private val SEGMENT_G = listOf(
        Point(SEGMENT_LEFT + SEGMENT_GAP, SEGMENT_MIDDLE),
        Point(SEGMENT_LEFT + SEGMENT_WIDTH + SEGMENT_GAP, SEGMENT_MIDDLE + SEGMENT_WIDTH / 2),
        Point(SEGMENT_RIGHT - SEGMENT_WIDTH - SEGMENT_GAP, SEGMENT_MIDDLE + SEGMENT_WIDTH / 2),
        Point(SEGMENT_RIGHT - SEGMENT_GAP, SEGMENT_MIDDLE),
        Point(SEGMENT_RIGHT - SEGMENT_WIDTH - SEGMENT_GAP, SEGMENT_MIDDLE - SEGMENT_WIDTH / 2),
        Point(SEGMENT_LEFT + SEGMENT_WIDTH + SEGMENT_GAP, SEGMENT_MIDDLE - SEGMENT_WIDTH / 2)
    )

    private val NUMBER_ZERO = listOf(
        SEGMENT_A,
        SEGMENT_B,
        SEGMENT_C,
        SEGMENT_D,
        SEGMENT_E,
        SEGMENT_F
    )
    private val NUMBER_ONE = listOf(
        SEGMENT_B,
        SEGMENT_C
    )
    private val NUMBER_TWO = listOf(
        SEGMENT_A,
        SEGMENT_B,
        SEGMENT_D,
        SEGMENT_E,
        SEGMENT_G
    )
    private val NUMBER_THREE = listOf(
        SEGMENT_A,
        SEGMENT_B,
        SEGMENT_C,
        SEGMENT_D,
        SEGMENT_G
    )
    private val NUMBER_FOUR = listOf(
        SEGMENT_B,
        SEGMENT_C,
        SEGMENT_F,
        SEGMENT_G
    )
    private val NUMBER_FIVE = listOf(
        SEGMENT_A,
        SEGMENT_C,
        SEGMENT_D,
        SEGMENT_F,
        SEGMENT_G
    )
    private val NUMBER_SIX = listOf(
        SEGMENT_A,
        SEGMENT_C,
        SEGMENT_D,
        SEGMENT_E,
        SEGMENT_F,
        SEGMENT_G
    )
    private val NUMBER_SEVEN = listOf(
        SEGMENT_A,
        SEGMENT_B,
        SEGMENT_C
    )
    private val NUMBER_EIGHT = listOf(
        SEGMENT_A,
        SEGMENT_B,
        SEGMENT_C,
        SEGMENT_D,
        SEGMENT_E,
        SEGMENT_F,
        SEGMENT_G
    )
    private val NUMBER_NINE = listOf(
        SEGMENT_A,
        SEGMENT_B,
        SEGMENT_C,
        SEGMENT_D,
        SEGMENT_F,
        SEGMENT_G
    )

    private fun drawSegments(
        canvas: ICanvas,
        segments: List<List<Point>>,
        color: Color,
        transformation: (Point) -> Point = { it }
    ) {
        for (segment in segments) {
            canvas.fillPolygon(segment.map(transformation), color)
        }
    }

    fun drawCharacter(canvas: ICanvas, character: Char, color: Color, transformation: (Point) -> Point = { it }) {
        val segments = when (character.toInt()) {
            '0'.toInt() -> NUMBER_ZERO
            '1'.toInt() -> NUMBER_ONE
            '2'.toInt() -> NUMBER_TWO
            '3'.toInt() -> NUMBER_THREE
            '4'.toInt() -> NUMBER_FOUR
            '5'.toInt() -> NUMBER_FIVE
            '6'.toInt() -> NUMBER_SIX
            '7'.toInt() -> NUMBER_SEVEN
            '8'.toInt() -> NUMBER_EIGHT
            '9'.toInt() -> NUMBER_NINE
            else -> emptyList()
        }
        drawSegments(canvas, segments, color, transformation)
    }

    fun drawCharacter(canvas: ICanvas, character: Char, color: Color, position: Point, size: Double) {
        drawCharacter(canvas, character, color) { it * size + position }
    }
}
