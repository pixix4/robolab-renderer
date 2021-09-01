package de.robolab.common.planet

import de.robolab.common.planet.utils.IPlanetValue
import de.robolab.common.utils.Vector
import kotlinx.serialization.Serializable
import kotlin.math.roundToLong

@Serializable
data class PlanetPoint(
    val x: Long,
    val y: Long,
) : IPlanetValue<PlanetPoint> {

    val point: Vector
        get() = Vector(x, y)

    override fun translate(delta: PlanetPoint) = Vector(x, y).plus(delta.point).let { p ->
        copy(x = p.x.roundToLong(), y = p.y.roundToLong())
    }

    override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint) =
        Vector(x, y).rotate(direction.angle, origin.point).let { p ->
            copy(x = p.x.roundToLong(), y = p.y.roundToLong())
        }

    fun getColor(bluePoint: PlanetPoint?): Color {
        if (bluePoint == null) {
            return Color.Unknown
        }

        if (((this.x + this.y) % 2L == 0L) == ((bluePoint.x + bluePoint.y) % 2L == 0L)) {
            return Color.Blue
        }
        return Color.Red
    }

    enum class Color {
        Red, Blue, Unknown
    }
}

val Vector.planetPoint: PlanetPoint
    get() = PlanetPoint(x.roundToLong(), y.roundToLong())
