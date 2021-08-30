package de.robolab.common.utils

import de.robolab.client.renderer.transition.IInterpolatable
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

data class Rectangle(
    val left: Double,
    val top: Double,
    val width: Double,
    val height: Double
) : IInterpolatable<Rectangle> {

    val right: Double by lazy { left + width }
    val bottom: Double by lazy { top + height }
    val center: Vector by lazy { Vector(left + width / 2, top + height / 2) }

    val bottomLeft: Vector by lazy { Vector(left, bottom) }
    val bottomRight: Vector by lazy { Vector(right, bottom) }
    val topLeft: Vector by lazy { Vector(left, top) }
    val topRight: Vector by lazy { Vector(right, top) }

    infix fun intersects(other: Rectangle): Boolean {
        return other.right > left && other.bottom > top && other.left < right && other.top < bottom
    }

    operator fun contains(point: Vector): Boolean {
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
        Vector(left, top),
        Vector(right, top),
        Vector(right, bottom),
        Vector(left, bottom)
    )


    fun rounded() = Rectangle(
        round(left),
        round(top),
        round(right) - round(left),
        round(bottom) - round(top),
    )
    fun roundedWithMultiplier(multiplier: Double = 1.0) = Rectangle(
        round(left * multiplier) / multiplier,
        round(top * multiplier) / multiplier,
        round(right * multiplier) / multiplier - round(left * multiplier) / multiplier,
        round(bottom * multiplier) / multiplier - round(top * multiplier) / multiplier
    )


    companion object {
        val ZERO = Rectangle(0.0, 0.0, 0.0, 0.0)

        fun fromEdges(vararg points: Vector): Rectangle {
            if (points.isEmpty()) return ZERO

            val min = points.reduce { acc, point ->
                Vector(
                    min(acc.left, point.left),
                    min(acc.top, point.top)
                )
            }
            val max = points.reduce { acc, point ->
                Vector(
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

        fun fromEdges(points: List<Vector>): Rectangle {
            return fromEdges(*points.toTypedArray())
        }

        fun fromDimension(origin: Vector, size: Dimension): Rectangle {
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

        fun fromCenter(center: Vector, width: Double, height: Double): Rectangle {
            return Rectangle(
                center.left - width / 2,
                center.top - height / 2,
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
