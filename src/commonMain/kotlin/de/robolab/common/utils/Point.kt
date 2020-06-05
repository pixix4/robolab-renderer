package de.robolab.common.utils

import de.robolab.client.renderer.transition.IInterpolatable
import de.robolab.common.planet.Coordinate
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Point(
        val left: Double,
        val top: Double
) : IInterpolatable<Point> {

    constructor(left: Number, top: Number) : this(left.toDouble(), top.toDouble())
    constructor(point: Pair<Number, Number>) : this(point.first.toDouble(), point.second.toDouble())
    constructor(point: Coordinate) : this(point.x.toDouble(), point.y.toDouble())

    operator fun plus(other: Point) = Point(left + other.left, top + other.top)
    operator fun minus(other: Point) = Point(left - other.left, top - other.top)
    operator fun times(factor: Number) = Point(left * factor.toDouble(), top * factor.toDouble())
    operator fun times(other: Point) = Point(left * other.left, top * other.top)
    operator fun div(factor: Number) = Point(left / factor.toDouble(), top / factor.toDouble())
    operator fun div(other: Point) = Point(left / other.left, top / other.top)
    operator fun unaryMinus() = Point(-left, -top)
    operator fun unaryPlus() = this
    operator fun compareTo(other: Point) = (left + top).compareTo(other.left + other.top)

    infix fun distanceTo(other: Point): Double {
        val l = left - other.left
        val r = top - other.top
        return sqrt(l * l + r * r)
    }

    infix fun manhattanDistanceTo(other: Point): Double {
        return abs(left - other.left) + abs(top - other.top)
    }

    fun midpoint(other: Point) = Point(
            (left + other.left) / 2,
            (top + other.top) / 2
    )

    fun magnitude() = sqrt(left * left + top * top)

    fun normalize(): Point {
        val magnitude = magnitude()

        return if (magnitude == 0.0) {
            ZERO
        } else {
            this / magnitude
        }
    }

    override fun interpolate(toValue: Point, progress: Double): Point {
        if (progress == 1.0) return toValue
        if (progress == 0.0) return this
        
        return Point(
                left * (1 - progress) + toValue.left * progress,
                top * (1 - progress) + toValue.top * progress
        )
    }

    override fun interpolateToNull(progress: Double): Point {
        return this
    }

    fun orthogonal() = Point(-y, x)
    
    fun rotate(rotation: Double, origin: Point = ZERO): Point {
        return if (origin == ZERO) {
            Point(
                left * cos(rotation) - top * sin(rotation),
                left * sin(rotation) + top * cos(rotation)
            )
        } else {
            (this - origin).rotate(rotation) + origin
        }
    }

    fun inverse() = Point(-x, -y)

    infix fun dotProduct(other: Point) = left * other.left + top * other.top

    infix fun projectOnto(basis: Point): Pair<Double, Point> {
        val distance = (this dotProduct basis) / (basis.left * basis.left + basis.top * basis.top)
        return distance to (basis * distance)
    }

    fun max(other: Point): Point = Point(
            kotlin.math.max(left, other.left),
            kotlin.math.max(top, other.top)
    )

    fun min(other: Point): Point = Point(
            kotlin.math.min(left, other.left),
            kotlin.math.min(top, other.top)
    )

    val width: Double
        get() = left

    val height: Double
        get() = top

    val x: Double
        get() = left

    val y: Double
        get() = top

    companion object {
        val ZERO = Point(0.0, 0.0)
        val ONE = Point(1.0, 1.0)
    }
}

typealias Dimension = Point
