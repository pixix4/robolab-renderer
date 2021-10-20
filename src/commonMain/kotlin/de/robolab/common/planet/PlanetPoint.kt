package de.robolab.common.planet

import de.robolab.client.repl.base.IReplCommandParameter
import de.robolab.client.repl.base.IReplCommandParameterTypeDescriptor
import de.robolab.common.planet.utils.IPlanetValue
import de.robolab.common.utils.Vector
import kotlinx.serialization.Serializable
import kotlin.math.roundToLong
import kotlin.reflect.KClass

@Serializable
data class PlanetPoint(
    val x: Long,
    val y: Long,
) : IPlanetValue<PlanetPoint>, IReplCommandParameter {

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

    override fun toToken(): String = "$x,$y"

    companion object : IReplCommandParameterTypeDescriptor<PlanetPoint> {
        override val klazz: KClass<PlanetPoint> = PlanetPoint::class
        override val description: String = "A point in planet-coordinates"
        override val example: List<String> = listOf("5,6")
        override val name: String = "Point"
        override val pattern: String = "<x>,<y>"
        override val regex: Regex = "^(-?\\d+),(-?\\d+)$".toRegex()
        override fun fromToken(token: String, match: MatchResult): PlanetPoint = PlanetPoint(
            match.groups[1]!!.value.toLong(),
            match.groups[2]!!.value.toLong()
        )
    }
}

val Vector.planetPoint: PlanetPoint
    get() = PlanetPoint(x.roundToLong(), y.roundToLong())
