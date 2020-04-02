package de.robolab.renderer.data

import de.robolab.model.Coordinate
import de.robolab.renderer.animation.IInterpolatable
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
    operator fun unaryMinus() = Point(-left, -top)
    operator fun unaryPlus() = this
    operator fun compareTo(other: Point) = (left + top).compareTo(other.left + other.top)

    fun distance(other: Point): Double {
        val l = left - other.left
        val r = top - other.top
        return sqrt(l * l + r * r)
    }

    fun manhattan_distance(other: Point): Double {
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

    override fun interpolate(toValue: Point, progress: Double) = Point(
            left * (1 - progress) + toValue.left * progress,
            top * (1 - progress) + toValue.top * progress
    )

    fun rotate(rotation: Double) = Point(
            left * cos(rotation) - top * sin(rotation),
            left * sin(rotation) + top * cos(rotation)
    )

    fun dotProduct(other: Point) = left * other.left + top * other.top

    fun projectOnto(basis: Point): Point {
        val distance = this.dotProduct(basis) / (basis.left * basis.left + basis.top * basis.top)
        return basis * distance
    }

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
