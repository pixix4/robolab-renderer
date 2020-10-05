package de.robolab.client.renderer.drawable.utils

import de.robolab.client.renderer.drawable.live.toAngle
import de.robolab.common.planet.Direction
import de.robolab.common.planet.PlanetVersion
import de.robolab.common.utils.Line
import de.robolab.common.utils.Point
import de.robolab.common.utils.Vector
import kotlin.math.*

object PathGenerator {

    private val generatorList = listOf(
        OuterLoopGenerator,
        InnerLoopGenerator,
        StraightLineGenerator
    )

    fun generateControlPoints(
        version: PlanetVersion,
        startPoint: Point,
        startDirection: Direction,
        endPoint: Point,
        endDirection: Direction
    ): List<Point> {
        val source = Line(startPoint, startDirection)
        val target = Line(endPoint, endDirection)

        for (generator in generatorList) {
            if (version >= generator.requiredVersion && generator.testGenerator(source, target)) {
                return generator.generate(source, target)?.removeDuplicates() ?: continue
            }
        }

        return DefaultGenerator.generate(source, target).removeDuplicates()
    }

    interface Generator {

        val requiredVersion: PlanetVersion

        fun testGenerator(source: Line, target: Line): Boolean

        fun generate(source: Line, target: Line): List<Point>?
    }

    private fun interpolateRotation(
        center: Point,
        startPoint: Point,
        endPoint: Point,
        progress: Double,
        isLarge: Boolean
    ): Point {
        val v = (startPoint - center)

        val start = v.toAngle()
        val end = (endPoint - center).toAngle()

        var diff = (end - start)
        if (diff < 0.0) {
            diff += 2 * PI
        }

        if (isLarge && diff < PI || !isLarge && diff > PI) {
            diff -= 2 * PI
        }

        val rotation = diff * progress

        return center + v.rotate(rotation)
    }

    fun interpolateRotation(start: Line, radius: Double, extend: Double): Pair<Line, List<Point>> {
        val center = start.rotate(PI / 2).moveOriginBy(radius).origin

        val stepCount = (extend.absoluteValue / PI * 8.0).roundToInt()
        val extendSign = extend.withSign(radius)
        val rVec = start.origin - center

        val list = mutableListOf<Vector>()
        for (i in 1..stepCount) {
            val vec = rVec.rotate(extendSign * i / stepCount.toDouble())
            list += center + vec
        }

        return Pair(
            Line(
                center + rVec.rotate(extendSign),
                start.direction.rotate(extendSign)
            ),
            list
        )
    }

    object OuterLoopGenerator : Generator {

        override val requiredVersion = PlanetVersion.V2020_FALL

        override fun testGenerator(source: Line, target: Line): Boolean {
            val cross = source.intersection(target).point ?: return false

            val (f1, _) = source.projectOnto(cross)
            val (f2, _) = target.projectOnto(cross)

            if (f1 < 0 || f2 < 0) return false

            return source.origin == target.origin
        }

        override fun generate(source: Line, target: Line): List<Point>? {
            val radius = 0.35
            val l1 = source.moveOriginBy(radius)
            val l2 = target.moveOriginBy(radius)

            val cross = l1.orthogonalIntersection(l2).point ?: return null

            val steps = 8
            val circlePoints = (0..steps).map { i ->
                interpolateRotation(cross, l1.origin, l2.origin, i.toDouble() / steps, true)
            }.toTypedArray()

            return listOf(
                source.moveOriginBy(0.25).origin,
                *circlePoints,
                target.moveOriginBy(0.25).origin
            )
        }
    }

    object InnerLoopGenerator : Generator {

        override val requiredVersion = PlanetVersion.V2020_FALL

        override fun testGenerator(source: Line, target: Line): Boolean {
            val cross = source.intersection(target).point ?: return false

            val (f1, _) = source.projectOnto(cross)
            val (f2, _) = target.projectOnto(cross)

            return f1 > 0 && f2 > 0
        }

        override fun generate(source: Line, target: Line): List<Point>? {

            val intersection = source.intersection(target).point ?: return null
            val radius = min(
                intersection distanceTo source.origin,
                intersection distanceTo target.origin
            ) - 0.25

            val l1Offset = (intersection distanceTo source.origin) - radius
            val l2Offset = (intersection distanceTo target.origin) - radius
            val l1 = source.moveOriginBy(l1Offset)
            val l2 = target.moveOriginBy(l2Offset)

            val cross = l1.orthogonalIntersection(l2).point ?: return null

            val steps = 8
            val circlePoints = (0..steps).map { i ->
                interpolateRotation(cross, l1.origin, l2.origin, i.toDouble() / steps, false)
            }.toTypedArray()

            return listOf(
                source.moveOriginBy(0.25).origin,
                source.moveOriginBy(l1Offset).origin,
                *circlePoints,
                target.moveOriginBy(l2Offset).origin,
                target.moveOriginBy(0.25).origin
            )
        }
    }

    object StraightLineGenerator : Generator {

        override val requiredVersion = PlanetVersion.V2020_FALL

        override fun testGenerator(source: Line, target: Line): Boolean {
            return source.origin != target.origin &&
                    source.intersection(target) is Line.Intersection.Identity &&
                    source.sameDirection(target)
        }

        override fun generate(source: Line, target: Line): List<Point>? {
            val (h, _) = source.projectOnto(target.origin)

            var front: Line
            var back: Line
            val reversed = h > 0.0
            if (reversed) {
                front = target
                back = source
            } else {
                front = source
                back = target
            }

            val list = mutableListOf<Vector>()

            front = front.moveOriginBy(0.25)
            list += front.origin

            interpolateRotation(front, 0.25, PI).let { (line, points) ->
                front = line
                list += points
            }

            front = front.moveOriginBy(0.3)
            list += front.origin

            interpolateRotation(front, 0.25, PI / 2).let { (line, points) ->
                front = line
                list += points
            }
            interpolateRotation(front, -0.25, PI / 2).let { (line, points) ->
                front = line
                list += points
            }

            back = back.moveOriginBy(0.25)
            list += back.origin

            if (reversed) {
                list.reverse()
            }

            return list
        }
    }

    object DefaultGenerator : Generator {

        override val requiredVersion = PlanetVersion.UNKNOWN

        override fun testGenerator(source: Line, target: Line): Boolean {
            return true
        }

        override fun generate(source: Line, target: Line): List<Point> {
            val firstList = generateControlPointsPart(
                PointVector(source.origin, source.direction, Vector::turnHigh, Vector::turnLow),
                PointVector(target.origin, target.direction, Vector::turnHigh, Vector::turnLow)
            )
            val secondList = generateControlPointsPart(
                PointVector(target.origin, target.direction, Vector::turnLow, Vector::turnHigh),
                PointVector(source.origin, source.direction, Vector::turnLow, Vector::turnHigh)
            )
            return firstList.zip(secondList.asReversed()) { a, b ->
                a.midpoint(b)
            }
        }

        private fun generateControlPointsPart(start: PointVector, end: PointVector): List<Point> {
            val startList = mutableListOf<Point>()
            val endList = mutableListOf<Point>()

            start.adder = startList::add
            end.adder = endList::add

            start.move()
            end.move()

            var i = 0
            while (start squareDistance end > PointVector.STEP_WIDTH && i < PointVector.MAX_ITERATIONS) {
                start.moveTo(end.point)
                end.moveTo(start.point)
                ++i
            }

            val s = startList.lastOrNull() ?: start.point
            val e = endList.firstOrNull() ?: end.point

            if (s.x != e.x && s.y != e.y || s.distanceTo(e) > 1) {
                val s1 = start.point
                val e1 = end.point
                val midpoint = e1.midpoint(s1)
                if (midpoint !in startList && midpoint !in endList) {
                    startList += midpoint
                }
            }

            return startList + endList.reversed()
        }

        class PointVector(
            var point: Point,
            var direction: Vector,
            val turnHigh: (Vector) -> Vector,
            val turnLow: (Vector) -> Vector
        ) {
            lateinit var adder: (Point) -> Boolean

            private fun Point.move(vector: Vector): Point {
                return this + vector * STEP_WIDTH
            }

            fun move() {
                point = point.move(direction)
            }

            fun moveTo(other: Point) {
                val forward = point.move(direction)

                val firstDirection = turnHigh(direction)
                val firstSide = point.move(firstDirection)

                val secondDirection = turnLow(direction)
                val secondSide = point.move(secondDirection)

                val next = listOf(forward, firstSide, secondSide).minByOrNull { it.squareDistance(other) }

                when (next) {
                    forward -> {
                        adder(point)
                    }
                    firstSide -> {
                        adder(point)
                        direction = firstDirection
                    }
                    secondSide -> {
                        adder(point)
                        direction = secondDirection
                    }
                }

                point = next ?: return
            }

            private infix fun Point.squareDistance(other: Point): Double {
                val x = x - other.x
                val y = y - other.y
                return x * x + y * y
            }

            infix fun squareDistance(other: PointVector): Double = point squareDistance other.point

            override fun equals(other: Any?): Boolean {
                if (this === other) return true

                if (other == null) return false
                if (this::class != other::class) return false

                other as PointVector

                if (point != other.point) return false

                return true
            }

            override fun hashCode(): Int {
                return point.hashCode()
            }

            companion object {
                const val STEP_WIDTH = 0.7

                const val MAX_ITERATIONS = 10
            }
        }
    }
}

fun Point.shift(direction: Direction, length: Double): Point {
    return when (direction) {
        Direction.NORTH -> Point(left, top + length)
        Direction.EAST -> Point(left + length, top)
        Direction.SOUTH -> Point(left, top - length)
        Direction.WEST -> Point(left - length, top)
    }
}

fun Direction.turnHigh() = when (this) {
    Direction.NORTH -> Direction.EAST
    Direction.EAST -> Direction.NORTH
    Direction.SOUTH -> Direction.WEST
    Direction.WEST -> Direction.SOUTH
}

fun Direction.turnLow() = when (this) {
    Direction.NORTH -> Direction.WEST
    Direction.EAST -> Direction.SOUTH
    Direction.SOUTH -> Direction.EAST
    Direction.WEST -> Direction.NORTH
}

fun Vector.turnHigh() = Direction.fromVector(this).turnHigh().toVector()

fun Vector.turnLow() = Direction.fromVector(this).turnLow().toVector()

fun List<Vector>.removeDuplicates(): List<Vector> {
    if (isEmpty()) return this

    val iterator = iterator()
    var last = iterator.next()

    val list = mutableListOf(last)

    for (element in iterator) {
        if (element distanceTo last > 0.01) {
            last = element
            list += element
        }
    }

    return list
}

fun Double.normalizeRadiant(): Double {
    val h = this % (2.0 * PI)
    if (h < 0) return  h + 2.0 * PI
    return h
}
fun Double.normalizeDegree(): Double {
    val h = this % 360.0
    if (h < 0) return  h + 360.0
    return h
}

fun Double.radiantToDegree() = this / PI * 180.0
fun Double.degreeToRadiant() = this / 180.0 * PI
