package de.robolab.common.utils

import de.robolab.client.renderer.transition.IInterpolatable
import de.robolab.common.parser.toFixed
import de.robolab.common.planet.Coordinate
import kotlin.math.*

data class Vector(
        val left: Double,
        val top: Double
) : IInterpolatable<Vector> {

    constructor(left: Number, top: Number) : this(left.toDouble(), top.toDouble())
    constructor(point: Pair<Number, Number>) : this(point.first.toDouble(), point.second.toDouble())
    constructor(point: Coordinate) : this(point.x.toDouble(), point.y.toDouble())

    operator fun plus(other: Vector) = Vector(left + other.left, top + other.top)
    operator fun minus(other: Vector) = Vector(left - other.left, top - other.top)
    operator fun times(factor: Number) = Vector(left * factor.toDouble(), top * factor.toDouble())
    operator fun times(other: Vector) = Vector(left * other.left, top * other.top)
    operator fun div(factor: Number) = Vector(left / factor.toDouble(), top / factor.toDouble())
    operator fun div(other: Vector) = Vector(left / other.left, top / other.top)
    operator fun unaryMinus() = Vector(-left, -top)
    operator fun unaryPlus() = this
    operator fun compareTo(other: Vector) = (left + top).compareTo(other.left + other.top)

    infix fun distanceTo(other: Vector): Double {
        val l = left - other.left
        val r = top - other.top
        return sqrt(l * l + r * r)
    }

    infix fun manhattanDistanceTo(other: Vector): Double {
        return abs(left - other.left) + abs(top - other.top)
    }

    fun midpoint(other: Vector) = Vector(
            (left + other.left) / 2,
            (top + other.top) / 2
    )

    fun magnitude() = sqrt(left * left + top * top)

    fun normalize(): Vector {
        val magnitude = magnitude()

        return if (magnitude == 0.0) {
            ZERO
        } else {
            this / magnitude
        }
    }

    override fun interpolate(toValue: Vector, progress: Double): Vector {
        if (progress == 1.0) return toValue
        if (progress == 0.0) return this
        
        return Vector(
                left * (1 - progress) + toValue.left * progress,
                top * (1 - progress) + toValue.top * progress
        )
    }

    override fun interpolateToNull(progress: Double): Vector {
        return this
    }

    fun orthogonal() = Vector(-y, x)
    
    fun rotate(rotation: Double, origin: Vector = ZERO): Vector {
        return if (origin == ZERO) {
            Vector(
                left * cos(rotation) - top * sin(rotation),
                left * sin(rotation) + top * cos(rotation)
            )
        } else {
            (this - origin).rotate(rotation) + origin
        }
    }

    fun inverse() = Vector(-x, -y)

    infix fun dotProduct(other: Vector) = left * other.left + top * other.top

    infix fun projectOnto(basis: Vector): Pair<Double, Vector> {
        val factor = (this dotProduct basis) / (basis.left * basis.left + basis.top * basis.top)
        return factor to (basis * factor)
    }

    fun max(other: Vector): Vector = Vector(
            kotlin.math.max(left, other.left),
            kotlin.math.max(top, other.top)
    )

    fun min(other: Vector): Vector = Vector(
            kotlin.math.min(left, other.left),
            kotlin.math.min(top, other.top)
    )

    fun rounded() = Vector(round(left), round(top))
    fun roundedWithMultiplier(multiplier: Double = 1.0) = Vector(round(left * multiplier) / multiplier, round(top * multiplier) / multiplier)

    override fun toString(): String {
        return "Vector(${left.toFixed(2)}, ${top.toFixed(2)})"
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
        val ZERO = Vector(0.0, 0.0)
        val ONE = Vector(1.0, 1.0)
    }
}

typealias Dimension = Vector
typealias Point = Vector
