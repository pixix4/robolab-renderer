package de.robolab.client.renderer.drawable.utils

import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.common.utils.Color
import de.robolab.common.utils.Vector

object SegmentDrawer {
    private const val SEGMENT_GAP = 0.02
    private const val SEGMENT_WIDTH = 0.15
    private const val SEGMENT_LEFT = 0.2
    private const val SEGMENT_RIGHT = 0.8
    private const val SEGMENT_TOP = 1.0
    private const val SEGMENT_MIDDLE = 0.5
    private const val SEGMENT_BOTTOM = 0.0

    private val SEGMENT_A = listOf(
        Vector(SEGMENT_LEFT + SEGMENT_GAP, SEGMENT_TOP),
        Vector(SEGMENT_RIGHT - SEGMENT_GAP, SEGMENT_TOP),
        Vector(SEGMENT_RIGHT - SEGMENT_WIDTH - SEGMENT_GAP, SEGMENT_TOP - SEGMENT_WIDTH),
        Vector(SEGMENT_LEFT + SEGMENT_WIDTH + SEGMENT_GAP, SEGMENT_TOP - SEGMENT_WIDTH)
    )
    private val SEGMENT_B = listOf(
        Vector(SEGMENT_RIGHT, SEGMENT_TOP - SEGMENT_GAP),
        Vector(SEGMENT_RIGHT, SEGMENT_MIDDLE + SEGMENT_GAP),
        Vector(SEGMENT_RIGHT - SEGMENT_WIDTH, SEGMENT_MIDDLE + SEGMENT_WIDTH / 2 + SEGMENT_GAP),
        Vector(SEGMENT_RIGHT - SEGMENT_WIDTH, SEGMENT_TOP - SEGMENT_WIDTH - SEGMENT_GAP)
    )
    private val SEGMENT_C = listOf(
        Vector(SEGMENT_RIGHT, SEGMENT_MIDDLE - SEGMENT_GAP),
        Vector(SEGMENT_RIGHT, SEGMENT_BOTTOM + SEGMENT_GAP),
        Vector(SEGMENT_RIGHT - SEGMENT_WIDTH, SEGMENT_BOTTOM + SEGMENT_WIDTH + SEGMENT_GAP),
        Vector(SEGMENT_RIGHT - SEGMENT_WIDTH, SEGMENT_MIDDLE - SEGMENT_WIDTH / 2 - SEGMENT_GAP)
    )
    private val SEGMENT_D = listOf(
        Vector(SEGMENT_LEFT + SEGMENT_GAP, SEGMENT_BOTTOM),
        Vector(SEGMENT_RIGHT - SEGMENT_GAP, SEGMENT_BOTTOM),
        Vector(SEGMENT_RIGHT - SEGMENT_WIDTH - SEGMENT_GAP, SEGMENT_BOTTOM + SEGMENT_WIDTH),
        Vector(SEGMENT_LEFT + SEGMENT_WIDTH + SEGMENT_GAP, SEGMENT_BOTTOM + SEGMENT_WIDTH)
    )
    private val SEGMENT_E = listOf(
        Vector(SEGMENT_LEFT, SEGMENT_MIDDLE - SEGMENT_GAP),
        Vector(SEGMENT_LEFT, SEGMENT_BOTTOM + SEGMENT_GAP),
        Vector(SEGMENT_LEFT + SEGMENT_WIDTH, SEGMENT_BOTTOM + SEGMENT_WIDTH + SEGMENT_GAP),
        Vector(SEGMENT_LEFT + SEGMENT_WIDTH, SEGMENT_MIDDLE - SEGMENT_WIDTH / 2 - SEGMENT_GAP)
    )
    private val SEGMENT_F = listOf(
        Vector(SEGMENT_LEFT, SEGMENT_TOP - SEGMENT_GAP),
        Vector(SEGMENT_LEFT, SEGMENT_MIDDLE + SEGMENT_GAP),
        Vector(SEGMENT_LEFT + SEGMENT_WIDTH, SEGMENT_MIDDLE + SEGMENT_WIDTH / 2 + SEGMENT_GAP),
        Vector(SEGMENT_LEFT + SEGMENT_WIDTH, SEGMENT_TOP - SEGMENT_WIDTH - SEGMENT_GAP)
    )
    private val SEGMENT_G = listOf(
        Vector(SEGMENT_LEFT + SEGMENT_GAP, SEGMENT_MIDDLE),
        Vector(SEGMENT_LEFT + SEGMENT_WIDTH + SEGMENT_GAP, SEGMENT_MIDDLE + SEGMENT_WIDTH / 2),
        Vector(SEGMENT_RIGHT - SEGMENT_WIDTH - SEGMENT_GAP, SEGMENT_MIDDLE + SEGMENT_WIDTH / 2),
        Vector(SEGMENT_RIGHT - SEGMENT_GAP, SEGMENT_MIDDLE),
        Vector(SEGMENT_RIGHT - SEGMENT_WIDTH - SEGMENT_GAP, SEGMENT_MIDDLE - SEGMENT_WIDTH / 2),
        Vector(SEGMENT_LEFT + SEGMENT_WIDTH + SEGMENT_GAP, SEGMENT_MIDDLE - SEGMENT_WIDTH / 2)
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
        segments: List<List<Vector>>,
        color: Color,
        transformation: (Vector) -> Vector = { it }
    ) {
        for (segment in segments) {
            canvas.fillPolygon(segment.map(transformation), color)
        }
    }

    fun drawCharacter(canvas: ICanvas, character: Char, color: Color, transformation: (Vector) -> Vector = { it }) {
        val segments = when (character.code) {
            '0'.code -> NUMBER_ZERO
            '1'.code -> NUMBER_ONE
            '2'.code -> NUMBER_TWO
            '3'.code -> NUMBER_THREE
            '4'.code -> NUMBER_FOUR
            '5'.code -> NUMBER_FIVE
            '6'.code -> NUMBER_SIX
            '7'.code -> NUMBER_SEVEN
            '8'.code -> NUMBER_EIGHT
            '9'.code -> NUMBER_NINE
            else -> emptyList()
        }
        drawSegments(canvas, segments, color, transformation)
    }

    fun drawCharacter(canvas: ICanvas, character: Char, color: Color, position: Vector, size: Double) {
        drawCharacter(canvas, character, color) { it * size + position }
    }
}
