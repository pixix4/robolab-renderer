package de.robolab.common.planet.test

import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetDirection
import de.robolab.common.planet.PlanetPoint
import de.robolab.common.planet.utils.IPlanetValue
import de.robolab.common.utils.Vector
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToLong

@Serializable
sealed class PlanetSubscribableRef : IPlanetValue<PlanetSubscribableRef> {

    abstract val x: Long
    abstract val y: Long

    val point: PlanetPoint
        get() = PlanetPoint(x, y)

    @Serializable
    @SerialName("PATH")
    data class Path(
        override val x: Long,
        override val y: Long,
        val direction: PlanetDirection,
        val bidirectional: Boolean,
    ) : PlanetSubscribableRef() {

        override fun translate(delta: PlanetPoint) = Vector(x, y).plus(delta.point).let { p ->
            copy(
                x = p.x.roundToLong(),
                y = p.x.roundToLong()
            )
        }

        override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint) =
            Vector(x, y).rotate(direction.angle, origin.point).let { p ->
                copy(
                    direction = this.direction.rotate(direction, origin),
                    x = p.x.roundToLong(),
                    y = p.x.roundToLong()
                )
            }
    }

    @Serializable
    @SerialName("NODE")
    data class Node(
        override val x: Long,
        override val y: Long,
    ) : PlanetSubscribableRef() {

        override fun translate(delta: PlanetPoint) = Vector(x, y).plus(delta.point).let { p ->
            copy(
                x = p.x.roundToLong(),
                y = p.x.roundToLong()
            )
        }

        override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint) =
            Vector(x, y).rotate(direction.angle, origin.point).let { p ->
                copy(
                    x = p.x.roundToLong(),
                    y = p.x.roundToLong()
                )
            }
    }
}
