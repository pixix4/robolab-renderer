package de.robolab.client.utils

import de.robolab.common.planet.Direction
import de.robolab.common.planet.Path
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

enum class PathClassifier(val desc: String) {
    LINE_1("Line 1"), LINE_2("Line 2"), LINE_3("Line 3"), LINE_LONG("Long line"),
    CURVE_1_1("Simple curve"), CURVE_2_1("L curve"), CURVE_2_2("Large curve"), CURVE_3_1("Long L curve"), CURVE_3_2("Large L curve"),
    S_CURVE_1_1("Small S Curve"), S_CURVE_2_1("Long S Curve"),
    CIRCLE_270("Circle"), CIRCLE_180("Large circle"), HALF_CIRCLE("Half circle"),
    DEAD_END("Dead end"),
    HOOK_0_1("Hook 1"), HOOK_0_2("Hook 2"), HOOK_0_3("Hook 3"), HOOK_0_LONG("Long hook"),
    SIDE_HOOK_0_1("Side hook 1"), SIDE_HOOK_0_2("Side hook 2"), SIDE_HOOK_0_3("Side hook 3"), SIDE_HOOK_0_LONG("Long side hook"),
    OTHER("Other");

    companion object {
        fun classify(path: Path): PathClassifier {
            val xSpan = abs(path.source.x - path.target.x)
            val ySpan = abs(path.source.y - path.target.y)
            val length = xSpan + ySpan

            if (length == 0) {
                return when {
                    path.sourceDirection == path.targetDirection -> DEAD_END
                    path.sourceDirection.opposite() == path.targetDirection -> CIRCLE_180
                    else -> CIRCLE_270
                }
            }

            if (xSpan == 0 || ySpan == 0) {
                return when {
                    path.sourceDirection == path.targetDirection ->
                        if ((path.sourceDirection == Direction.EAST || path.sourceDirection == Direction.WEST) && xSpan == 0 ||
                                (path.sourceDirection == Direction.NORTH || path.sourceDirection == Direction.SOUTH) && ySpan == 0) {
                        HALF_CIRCLE
                    } else when (length) {
                        1 -> HOOK_0_1
                        2 -> HOOK_0_2
                        3 -> HOOK_0_3
                        else -> HOOK_0_LONG
                    }
                    path.sourceDirection.opposite() == path.targetDirection -> when (length) {
                        1 -> LINE_1
                        2 -> LINE_2
                        3 -> LINE_3
                        else -> LINE_LONG
                    }
                    else -> when (length) {
                        1 -> SIDE_HOOK_0_1
                        2 -> SIDE_HOOK_0_2
                        3 -> SIDE_HOOK_0_3
                        else -> SIDE_HOOK_0_LONG
                    }
                }
            }

            val minSpan = min(xSpan, ySpan)
            val maxSpan = max(xSpan, ySpan)

            return when {
                path.sourceDirection == path.targetDirection -> OTHER
                path.sourceDirection.opposite() == path.targetDirection -> when (maxSpan) {
                    1 -> S_CURVE_1_1
                    2 -> S_CURVE_2_1
                    else -> OTHER
                }
                else -> when (maxSpan) {
                    1 -> CURVE_1_1
                    2 -> when (minSpan) {
                        1 -> CURVE_2_1
                        else -> CURVE_2_2
                    }
                    3 -> when (minSpan) {
                        1 -> CURVE_3_1
                        else -> CURVE_3_2
                    }
                    else -> OTHER
                }
            }
        }
    }
}