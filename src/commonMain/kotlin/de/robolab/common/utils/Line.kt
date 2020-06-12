package de.robolab.common.utils

import de.robolab.common.planet.Direction


data class Line(
    val origin: Point,
    val direction: Vector
) {
    constructor(origin: Point, direction: Direction) : this(origin, direction.toVector())

    infix fun parallelTo(other: Line): Boolean {
        val d1 = direction.normalize()
        val d2 = other.direction.normalize()

        return d1 == d2 || d1.inverse() == d2
    }

    fun intersection(other: Line): Point? {
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

        val t = t1 / t2

        return Point(
            x1 + t * (x2 - x1),
            y1 + t * (y2 - y1)
        )
    }

    fun orthogonal() = Line(origin, direction.orthogonal())
    fun rotate(rotation: Double) = Line(origin, direction.rotate(rotation))

    infix fun projectOnto(point: Point): Pair<Double, Point> {
        val (factor, vector) = (point - origin) projectOnto direction
        return factor to (vector + origin)
    }

    fun orthogonalIntersection(other: Line): Point? {
        return orthogonal().intersection(other.orthogonal())
    }
}
