package de.robolab.common.utils

import de.robolab.client.renderer.transition.IInterpolatable
import kotlin.math.max
import kotlin.math.min

data class Rectangle(
    val left: Double,
    val top: Double,
    val width: Double,
    val height: Double
) : IInterpolatable<Rectangle> {

    val right: Double by lazy { left + width }
    val bottom: Double by lazy { top + height }
    val center: Point by lazy { Point(left + width / 2, top + height / 2) }

    val bottomLeft: Point by lazy { Point(left, bottom) }
    val bottomRight: Point by lazy { Point(right, bottom) }
    val topLeft: Point by lazy { Point(left, top) }
    val topRight: Point by lazy { Point(right, top) }

    infix fun intersects(other: Rectangle): Boolean {
        return other.right > left && other.bottom > top && other.left < right && other.top < bottom
    }

    operator fun contains(point: Point): Boolean {
        return point.left > left && point.top > top && point.left < right && point.top < bottom
    }

    operator fun contains(other: Rectangle) = (this union other) == this

    fun expand(size: Double) = expand(size, size)
    fun shrink(size: Double) = shrink(size, size)

    fun expand(vertical: Double, horizontal: Double) = Rectangle(
        left - horizontal,
        top - vertical,
        width + 2 * horizontal,
        height + 2 * vertical
    )

    fun shrink(vertical: Double, horizontal: Double) = expand(-vertical, -horizontal)

    infix fun union(other: Rectangle): Rectangle {
        return fromEdges(toEdgeList() + other.toEdgeList())
    }

    fun union(other: Rectangle, ignoreThreshold: Double): Rectangle {
        val union = union(other)
        return if (expand(ignoreThreshold).contains(union)) union else this
    }

    override fun interpolate(toValue: Rectangle, progress: Double) = Rectangle(
        left * (1 - progress) + toValue.left * progress,
        top * (1 - progress) + toValue.top * progress,
        width * (1 - progress) + toValue.width * progress,
        height * (1 - progress) + toValue.height * progress
    )

    override fun interpolateToNull(progress: Double): Rectangle {
        val centerRect = fromEdges(center)

        return interpolate(centerRect, progress)
    }

    fun toEdgeList() = listOf(
        Point(left, top),
        Point(right, top),
        Point(right, bottom),
        Point(left, bottom)
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
            var left = origin.left
            var top = origin.top
            var width = size.width
            var height = size.height

            if (width < 0.0) {
                left += width
                width *= -1 - 0
            }
            if (height < 0.0) {
                top += height
                height *= -1 - 0
            }

            return Rectangle(
                left,
                top,
                width,
                height
            )
        }
    }
}

infix fun Rectangle?.unionNullable(other: Rectangle?): Rectangle? {
    return (this ?: return other) union (other ?: return this)
}

val Rectangle.dimension
    get() = Dimension(width, height)

fun Rectangle.splitVertical(split: Double = 0.5): Pair<Rectangle, Rectangle> {
    return Pair(
        Rectangle(left, top, width, height * split),
        Rectangle(left, top + height * split, width, height * (1.0 - split))
    )
}

fun Rectangle.splitHorizontal(split: Double = 0.5): Pair<Rectangle, Rectangle> {
    return Pair(
        Rectangle(left, top, width * split, height),
        Rectangle(left + width * split, top, width * (1.0 - split), height)
    )
}
