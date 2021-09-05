package de.robolab.common.planet

import de.robolab.common.planet.utils.IPlanetValue
import de.robolab.common.planet.utils.rotate
import de.robolab.common.planet.utils.translate
import de.robolab.common.utils.Vector
import kotlinx.serialization.Serializable
import kotlin.math.roundToLong

@Serializable
data class PlanetPath(
    val sourceX: Long,
    val sourceY: Long,
    val sourceDirection: PlanetDirection,
    val targetX: Long,
    val targetY: Long,
    val targetDirection: PlanetDirection,
    val weight: Long,
    val exposure: Set<PlanetPathExposure>,
    val hidden: Boolean,
    val spline: PlanetSpline? = null,
    val arrow: Boolean = false,
) : IPlanetValue<PlanetPath> {

    constructor(
        source: PlanetPoint,
        sourceDirection: PlanetDirection,
        target: PlanetPoint,
        targetDirection: PlanetDirection,
        weight: Long,
        exposure: Set<PlanetPathExposure>,
        hidden: Boolean,
        spline: PlanetSpline? = null,
        arrow: Boolean = false,
    ) : this(
        source.x,
        source.y,
        sourceDirection,
        target.x,
        target.y,
        targetDirection,
        weight,
        exposure,
        hidden,
        spline,
        arrow
    )

    constructor(
        sourceVertex: PlanetPathVertex,
        targetVertex: PlanetPathVertex,
        weight: Long,
        exposure: Set<PlanetPathExposure>,
        hidden: Boolean,
        spline: PlanetSpline? = null,
        arrow: Boolean = false
    ) : this(
        sourceVertex.point,
        sourceVertex.direction,
        targetVertex.point,
        targetVertex.direction,
        weight,
        exposure,
        hidden,
        spline,
        arrow
    )

    val blocked = weight < 0

    val source: PlanetPoint
        get() = PlanetPoint(sourceX, sourceY)

    val target: PlanetPoint
        get() = PlanetPoint(targetX, targetY)

    val sourceVertex: PlanetPathVertex
        get() = PlanetPathVertex(source, sourceDirection)

    val targetVertex: PlanetPathVertex
        get() = PlanetPathVertex(target, targetDirection)

    fun length(spline: PlanetSpline? = this.spline): Double {
        if (spline == null) {
            return Vector(sourceX, sourceY) distanceTo Vector(targetX, targetY)
        }

        return spline.length(sourceX, sourceY, targetX, targetY)
    }

    fun length(controlPoints: List<PlanetCoordinate>): Double {
        return (listOf(Vector(sourceX, sourceY)) + controlPoints.map { it.point } + Vector(
            targetX,
            targetY
        )).windowed(2, 1)
            .sumOf { (p0, p1) -> p0.distanceTo(p1) }
    }

    fun length(controlPoints: List<Vector>): Double {
        return (listOf(Vector(sourceX, sourceY)) + controlPoints.map { it } + Vector(
            targetX,
            targetY
        )).windowed(2, 1)
            .sumOf { (p0, p1) -> p0.distanceTo(p1) }
    }

    val isOneWayPath: Boolean
        get() = sourceX == targetX && sourceY == targetY && sourceDirection == targetDirection

    fun equalPath(other: PlanetPath, checkReverse: Boolean = true): Boolean {
        if (sourceX == other.sourceX &&
            sourceY == other.sourceY &&
            sourceDirection == other.sourceDirection &&
            targetX == other.targetX &&
            targetY == other.targetY &&
            targetDirection == other.targetDirection
        ) {
            return true
        }

        if (checkReverse) {
            return equalPath(other.reversed(), false)
        }

        return false
    }

    fun connectsWith(point: PlanetPoint): Boolean {
        return (sourceX == point.x && sourceY == point.y) || (targetX == point.x && targetY == point.y)
    }

    fun connectsWith(point: PlanetPoint, direction: PlanetDirection): Boolean {
        return (sourceX == point.x && sourceY == point.y && direction == sourceDirection) || (targetX == point.x && targetY == point.y && targetDirection == direction)
    }

    fun reversed(): PlanetPath {
        return copy(
            sourceX = targetX,
            sourceY = targetY,
            sourceDirection = targetDirection,
            targetX = sourceX,
            targetY = sourceY,
            targetDirection = sourceDirection,
            spline = spline?.reversed()
        )
    }

    override fun translate(delta: PlanetPoint) = copy(
        sourceX = PlanetPoint(targetX, targetY).translate(delta).x,
        sourceY = PlanetPoint(targetX, targetY).translate(delta).y,
        sourceDirection = targetDirection.translate(delta),
        targetX = PlanetPoint(sourceX, sourceY).translate(delta).x,
        targetY = PlanetPoint(sourceX, sourceY).translate(delta).y,
        targetDirection = sourceDirection.translate(delta),
        spline = spline.translate(delta),
        exposure = exposure.translate(delta)
    )

    override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint) = copy(
        sourceX = PlanetPoint(targetX, targetY).rotate(direction, origin).x,
        sourceY = PlanetPoint(targetX, targetY).rotate(direction, origin).y,
        sourceDirection = targetDirection.rotate(direction, origin),
        targetX = PlanetPoint(sourceX, sourceY).rotate(direction, origin).x,
        targetY = PlanetPoint(sourceX, sourceY).rotate(direction, origin).y,
        targetDirection = sourceDirection.rotate(direction, origin),
        spline = spline.rotate(direction, origin),
        exposure = exposure.rotate(direction, origin)
    )

    override fun scaleWeights(factor: Double, offset: Long) = copy(
        weight = (weight * factor).roundToLong() + offset
    )

    fun splitPath(retainWeight: Boolean = false): Pair<PlanetPath, PlanetPath> {
        return copy(
            sourceX = sourceX,
            sourceY = sourceY,
            sourceDirection = sourceDirection,
            targetX = sourceX,
            targetY = sourceY,
            targetDirection = sourceDirection,
            weight = if (retainWeight) weight else -1,
            spline = null
        ) to copy(
            sourceX = targetX,
            sourceY = targetY,
            sourceDirection = targetDirection,
            targetX = targetX,
            targetY = targetY,
            targetDirection = targetDirection,
            weight = if (retainWeight) weight else -1,
            spline = null
        )

        //TODO: Add Spline splitting
    }
}
