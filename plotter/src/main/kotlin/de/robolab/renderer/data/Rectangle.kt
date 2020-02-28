package de.robolab.renderer.data

import kotlin.math.max
import kotlin.math.min

data class Rectangle(
        val left: Double,
        val top: Double,
        val width: Double,
        val height: Double
) {

    val right: Double by lazy { left + width }
    val bottom: Double by lazy { top + height }

    fun intersects(other: Rectangle): Boolean {
        return other.right > left && other.bottom > top && other.left < right && other.top < bottom;
    }

    fun expand(size: Double) = Rectangle(
            left - size,
            top - size,
            width + 2 * size,
            height + 2 * size
    )

    companion object {
        val ZERO = Rectangle(0.0, 0.0, 0.0, 0.0)

        fun fromEdges(vararg points: Point): Rectangle {
            if (points.isEmpty()) return ZERO

            val min = points.reduce { acc, point ->
                Point(
                        min(acc.left, point.left),
                        min(acc.top, point.top)
                )
            }
            val max = points.reduce { acc, point ->
                Point(
                        max(acc.left, point.left),
                        max(acc.top, point.top)
                )
            }

            return Rectangle(
                    min.left,
                    min.top,
                    max.left - min.left,
                    max.top - min.top
            )
        }

        fun fromEdges(points: List<Point>): Rectangle {
            return fromEdges(*points.toTypedArray())
        }

        fun fromDimension(origin: Point, size: Dimension): Rectangle {
            return Rectangle(
                    origin.left,
                    origin.top,
                    size.width,
                    size.height
            )
        }
    }
}