package de.robolab.common.utils

import de.robolab.common.parser.toFixed
import de.robolab.common.planet.Direction


data class Line(
    val origin: Point,
    val direction: Vector
) {
    constructor(origin: Point, direction: Direction) : this(origin, direction.toVector())

    fun intersection(other: Line): Intersection {
        val x1 = origin.x
        val y1 = origin.y
        val x2 = origin.x + direction.x
        val y2 = origin.y + direction.y
        val x3 = other.origin.x
        val y3 = other.origin.y
        val x4 = other.origin.x + other.direction.x
        val y4 = other.origin.y + other.direction.y

        val t1 = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4))
        val t2 = ((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4))

        if (t2 == 0.0) {
            return if (t1 == 0.0) {
                Intersection.Identity
            } else {
                Intersection.Parallel
            }
        }

        val t = t1 / t2

        return Intersection.Intersect(Point(
            x1 + t * (x2 - x1),
            y1 + t * (y2 - y1)
        ))
    }

    fun orthogonal() = Line(origin, direction.orthogonal())
    fun rotate(rotation: Double) = Line(origin, direction.rotate(rotation))

    infix fun projectOnto(point: Point): Pair<Double, Point> {
        val (factor, vector) = (point - origin) projectOnto direction
        return factor to (vector + origin)
    }

    fun orthogonalIntersection(other: Line): Intersection {
        return orthogonal().intersection(other.orthogonal())
    }

    infix fun parallelTo(other: Line): Boolean {
        return intersection(other) !is Intersection.Intersect
    }

    infix fun sameDirection(other: Line): Boolean {
        return direction.normalize() == other.direction.normalize()
    }

    fun moveOriginBy(distance: Double) = copy(origin = origin + direction.normalize() * distance)

    override fun toString(): String {
        val oX = origin.x.toFixed(2)
        val oY = origin.y.toFixed(2)
        val dX = direction.x.toFixed(2)
        val dY = direction.y.toFixed(2)

        return "Line(($oX, $oY) + t * ($dX, $dY))"
    }

    sealed class Intersection {
        object Parallel: Intersection()
        object Identity: Intersection()
        data class Intersect(override val point: Vector): Intersection()

        open val point: Vector? = null

        override fun toString(): String {
            return this::class.simpleName ?: super.toString()
        }
    }
}
