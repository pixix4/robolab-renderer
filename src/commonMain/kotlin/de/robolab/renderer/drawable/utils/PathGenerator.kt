package de.robolab.renderer.drawable.utils

import de.robolab.planet.Direction
import de.robolab.renderer.data.Point

object PathGenerator {
    fun generateControlPoints(startPoint: Point, startDirection: Direction, endPoint: Point, endDirection: Direction): List<Point> {
        val firstList = generateControlPointsPart(
                PointVector(startPoint, startDirection, Direction::turnHigh, Direction::turnLow),
                PointVector(endPoint, endDirection, Direction::turnHigh, Direction::turnLow)
        )
        val secondList = generateControlPointsPart(
                PointVector(endPoint, endDirection, Direction::turnLow, Direction::turnHigh),
                PointVector(startPoint, startDirection, Direction::turnLow, Direction::turnHigh)
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

        if (s.x != e.x && s.y != e.y || s.distance(e) > 1) {
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
            var direction: Direction,
            val turnHigh: (Direction) -> Direction,
            val turnLow: (Direction) -> Direction
    ) {
        lateinit var adder: (Point) -> Boolean

        private fun Point.move(direction: Direction): Point = when (direction) {
            Direction.NORTH -> this + Point(0.0, STEP_WIDTH)
            Direction.EAST -> this + Point(STEP_WIDTH, 0.0)
            Direction.SOUTH -> this + Point(0.0, -STEP_WIDTH)
            Direction.WEST -> this + Point(-STEP_WIDTH, 0.0)
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

            val next = listOf(forward, firstSide, secondSide).minBy { it.squareDistance(other) }

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

            // const val STEP_WIDTH_SQUARE = STEP_WIDTH * STEP_WIDTH
            const val MAX_ITERATIONS = 10
        }
    }

    fun isPointInDirectLine(start: Point, direction: Direction, target: Point): Boolean = when (direction) {
        Direction.NORTH -> start.y <= target.y && start.x == target.x
        Direction.EAST -> start.x <= target.x && start.y == target.y
        Direction.SOUTH -> start.y >= target.y && start.x == target.x
        Direction.WEST -> start.x >= target.x && start.y == target.y
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
