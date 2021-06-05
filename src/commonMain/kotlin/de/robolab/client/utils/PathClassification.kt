package de.robolab.client.utils

import de.robolab.client.renderer.drawable.general.PathAnimatable
import de.robolab.client.renderer.drawable.utils.BSpline
import de.robolab.client.renderer.drawable.utils.CurveEval
import de.robolab.client.renderer.drawable.utils.radiantToDegree
import de.robolab.common.planet.PlanetDirection
import de.robolab.common.planet.PlanetPath
import de.robolab.common.planet.utils.PlanetVersion
import de.robolab.common.utils.Vector
import de.robolab.common.utils.toFixed
import kotlin.math.*

data class PathClassification(
    val path: PlanetPath,
    val score: Int,
    val classifier: Classifier,
    val difficulty: Difficulty,
    val segments: List<Segment>,
    val explicitSegments: List<Segment>,
    val completeSegment: Segment,
    val table: String,
) {

    enum class SegmentType {
        LEFT, RIGHT, STRAIGHT;

        companion object {
            fun fromAngle(angle: Double): SegmentType = when {
                angle < -1e-3 -> RIGHT
                angle > 1e-3 -> LEFT
                else -> STRAIGHT
            }
        }
    }

    enum class Difficulty {
        EASY, MEDIUM, DIFFICULT, HARD;

        fun or(other: Difficulty): Difficulty {
            return values()[max(ordinal, other.ordinal)]
        }

        companion object {
            private const val MEDIUM_CURVINESS_THRESHOLD = 200.0
            private const val DIFFICULT_CURVINESS_THRESHOLD = 450.0
            private const val HARD_CURVINESS_THRESHOLD = 600.0

            fun create(segments: List<Segment>): Difficulty {
                var difficulty = EASY

                val sum = segments.reduce { a, b ->
                    a.merge(b)
                }

                if (sum.curviness >= MEDIUM_CURVINESS_THRESHOLD) {
                    difficulty = difficulty.or(MEDIUM)
                }
                if (sum.curviness >= DIFFICULT_CURVINESS_THRESHOLD) {
                    difficulty = difficulty.or(DIFFICULT)
                }
                if (sum.curviness >= HARD_CURVINESS_THRESHOLD) {
                    difficulty = difficulty.or(HARD)
                }

                val size = segments.count { it.distance > 0.1 }
                if (size >= 4) {
                    difficulty = difficulty.or(MEDIUM)
                }
                if (size >= 6) {
                    difficulty = difficulty.or(DIFFICULT)
                }
                if (size >= 8) {
                    difficulty = difficulty.or(HARD)
                }

                return difficulty
            }
        }
    }

    data class Segment(
        val angle: Double,
        val distance: Double,
        val startProgress: Double,
        val endProgress: Double,
        val curviness: Double = abs(angle / distance).radiantToDegree(),
        val type: SegmentType = SegmentType.fromAngle(angle),
    ) {

        fun merge(other: Segment): Segment {
            return if (type == other.type) Segment(
                angle + other.angle,
                distance + other.distance,
                min(startProgress, other.startProgress),
                max(endProgress, other.endProgress),
                max(curviness, other.curviness),
                type
            ) else Segment(
                angle + other.angle,
                distance + other.distance,
                startProgress,
                other.endProgress,
                max(curviness, other.curviness)
            )
        }

        fun fold(acc: List<Segment>): List<Segment> {
            return if (acc.lastOrNull()?.type == type) {
                val last = acc.last()
                acc.dropLast(1) + merge(last)
            } else {
                acc + this
            }
        }

        override fun toString(): String {
            val v0 = type.name.first()
            val v1 = angle.radiantToDegree().roundToInt().toString().padStart(5)
            val v2 = distance.toFixed(2).padStart(5)
            val v3 = curviness.roundToInt().toString().padStart(5)
            return "$v0: $v1, $v2, $v3"
        }

        companion object {
            fun create(
                v0: Vector?,
                v1: Vector,
                v2: Vector?,
                startProgress: Double,
                endProgress: Double,
            ): Segment {
                val distance = v1.magnitude()
                var angle = if (v0 == null) {
                    if (v2 == null) {
                        0.0
                    } else {
                        v1.angle(v2)
                    }
                } else {
                    if (v2 == null) {
                        v0.angle(v1)
                    } else {
                        v0.angle(v2)
                    }
                }

                if (angle >= PI) {
                    angle -= (2.0 * PI)
                } else if (angle < -PI) {
                    angle += (2.0 * PI)
                }

                if (v0 != null && v2 != null) {
                    angle /= 2.0
                }

                return Segment(angle, distance, startProgress, endProgress)
            }
        }
    }

    enum class Classifier(val desc: String) {
        LINE_1("Line 1"), LINE_2("Line 2"), LINE_3("Line 3"), LINE_LONG("Long line"),
        CURVE_1_1("Simple curve"), CURVE_2_1("L curve"), CURVE_2_2("Large curve"), CURVE_3_1("Long L curve"), CURVE_3_2(
            "Large L curve"
        ),
        S_CURVE_1_1("Small S Curve"), S_CURVE_2_1("Long S Curve"),
        CIRCLE_270("Circle"), CIRCLE_180("Large circle"), HALF_CIRCLE("Half circle"),
        DEAD_END("Dead end"),
        HOOK_0_1("Hook 1"), HOOK_0_2("Hook 2"), HOOK_0_3("Hook 3"), HOOK_0_LONG("Long hook"),
        SIDE_HOOK_0_1("Side hook 1"), SIDE_HOOK_0_2("Side hook 2"), SIDE_HOOK_0_3("Side hook 3"), SIDE_HOOK_0_LONG("Long side hook"),
        OTHER("Other");

        companion object {
            fun create(path: PlanetPath): Classifier {
                val xSpan = abs(path.source.x - path.target.x)
                val ySpan = abs(path.source.y - path.target.y)
                val length = xSpan + ySpan

                if (length == 0L) {
                    return when {
                        path.sourceDirection == path.targetDirection -> DEAD_END
                        path.sourceDirection.opposite() == path.targetDirection -> CIRCLE_180
                        else -> CIRCLE_270
                    }
                }

                if (xSpan == 0L || ySpan == 0L) {
                    return when {
                        path.sourceDirection == path.targetDirection ->
                            if ((path.sourceDirection == PlanetDirection.East || path.sourceDirection == PlanetDirection.West) && xSpan == 0L ||
                                (path.sourceDirection == PlanetDirection.North || path.sourceDirection == PlanetDirection.South) && ySpan == 0L
                            ) {
                                HALF_CIRCLE
                            } else when (length) {
                                1L -> HOOK_0_1
                                2L -> HOOK_0_2
                                3L -> HOOK_0_3
                                else -> HOOK_0_LONG
                            }
                        path.sourceDirection.opposite() == path.targetDirection -> when (length) {
                            1L -> LINE_1
                            2L -> LINE_2
                            3L -> LINE_3
                            else -> LINE_LONG
                        }
                        else -> when (length) {
                            1L -> SIDE_HOOK_0_1
                            2L -> SIDE_HOOK_0_2
                            3L -> SIDE_HOOK_0_3
                            else -> SIDE_HOOK_0_LONG
                        }
                    }
                }

                val minSpan = min(xSpan, ySpan)
                val maxSpan = max(xSpan, ySpan)

                return when {
                    path.sourceDirection == path.targetDirection -> OTHER
                    path.sourceDirection.opposite() == path.targetDirection -> when (maxSpan) {
                        1L -> S_CURVE_1_1
                        2L -> S_CURVE_2_1
                        else -> OTHER
                    }
                    else -> when (maxSpan) {
                        1L -> CURVE_1_1
                        2L -> when (minSpan) {
                            1L -> CURVE_2_1
                            else -> CURVE_2_2
                        }
                        3L -> when (minSpan) {
                            1L -> CURVE_3_1
                            else -> CURVE_3_2
                        }
                        else -> OTHER
                    }
                }
            }
        }

    }

    companion object {
        fun classify(planetVersion: Long, path: PlanetPath): PathClassification? {
            val controlPoints = PathAnimatable.getControlPointsFromPath(planetVersion, path)
            val lengthEstimate = path.length(controlPoints)
            val evalCount = (lengthEstimate * 10).roundToInt()

            val source = PathAnimatable.getSourcePointFromPath(path)
            val target = PathAnimatable.getTargetPointFromPath(planetVersion, path)

            val eval = CurveEval.evalSplineAttributed(
                evalCount,
                controlPoints,
                source,
                target,
                BSpline,
                1e-2
            )

            val linePoints = if (path.isOneWayPath && planetVersion < PlanetVersion.V2020_SPRING) eval.subList(
                0,
                eval.size / 2
            ) else eval

            val vectorList = linePoints
                .windowed(3, 1)
                .map { (p0, p1) -> p1.point - p0.point to Pair(p0.curveProgress, p1.curveProgress) }

            if (vectorList.size < 3) return null
            val explicitSegments = listOf(
                Segment.create(
                    null,
                    vectorList[0].first,
                    vectorList[1].first,
                    vectorList[0].second.first,
                    vectorList[0].second.second
                )
            ) + vectorList.windowed(3, 1)
                .map { (v0, v1, v2) ->
                    Segment.create(
                        v0.first,
                        v1.first,
                        v2.first,
                        v1.second.first,
                        v1.second.second
                    )
                } + Segment.create(
                vectorList[vectorList.lastIndex - 1].first,
                vectorList[vectorList.lastIndex].first,
                null,
                vectorList[vectorList.lastIndex].second.first,
                vectorList[vectorList.lastIndex].second.second
            )

            val segments = explicitSegments.fold(emptyList<Segment>()) { acc, s -> s.fold(acc) }

            val sum = segments.reduce { a, b ->
                a.merge(b)
            }
            val score = sum.curviness + segments.map { it.curviness }.average()
            val difficulty = Difficulty.create(segments)

            return PathClassification(
                path,
                score.roundToInt(),
                Classifier.create(path),
                difficulty,
                segments,
                explicitSegments,
                sum,
                segments.joinToString("\n") + "\n\n" + sum
            )
        }
    }
}

inline fun <T> Iterable<T>.removeConsecutiveDuplicateIf(predicate: (a: T, b: T) -> Boolean): List<T> {
    val list = ArrayList<T>()
    for (e in this) {
        if (list.isEmpty() || !predicate(list.last(), e)) {
            list += e
        }
    }
    return list
}

fun <T> Iterable<T>.removeConsecutiveDuplicateIf(): List<T> {
    return removeConsecutiveDuplicateIf { a, b -> a == b }
}
