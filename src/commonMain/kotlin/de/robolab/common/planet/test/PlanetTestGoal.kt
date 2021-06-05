package de.robolab.common.planet.test

import de.robolab.common.planet.Planet
import de.robolab.common.planet.PlanetPoint
import de.robolab.common.planet.utils.IPlanetValue
import de.robolab.common.utils.Vector
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToLong

@Serializable
sealed class PlanetTestGoal : IPlanetValue<PlanetTestGoal> {


    abstract val point: PlanetPoint?


    @Serializable
    @SerialName("EXPLORE")
    object Explore : PlanetTestGoal() {

        override val point: PlanetPoint? = null
    }

    @Serializable
    @SerialName("EXPLORE_COORDINATE")
    data class ExploreCoordinate(
        val x: Long,
        val y: Long,
    ) : PlanetTestGoal() {

        override val point: PlanetPoint
            get() = PlanetPoint(x, y)

        override fun translate(delta: PlanetPoint): PlanetTestGoal {
            return Vector(x, y).plus(delta.point).let { p ->
                copy(
                    x = p.x.roundToLong(),
                    y = p.x.roundToLong()
                )
            }
        }

        override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint): PlanetTestGoal {
            return Vector(x, y).rotate(direction.angle, origin.point).let { p ->
                copy(
                    x = p.x.roundToLong(),
                    y = p.x.roundToLong()
                )
            }
        }
    }

    @Serializable
    @SerialName("TARGET")
    data class Target(
        val x: Long,
        val y: Long,
    ) : PlanetTestGoal() {

        override val point: PlanetPoint
            get() = PlanetPoint(x, y)

        override fun translate(delta: PlanetPoint): PlanetTestGoal {
            return Vector(x, y).plus(delta.point).let { p ->
                copy(
                    x = p.x.roundToLong(),
                    y = p.x.roundToLong()
                )
            }
        }

        override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint): PlanetTestGoal {
            return Vector(x, y).rotate(direction.angle, origin.point).let { p ->
                copy(
                    x = p.x.roundToLong(),
                    y = p.x.roundToLong()
                )
            }
        }
    }
}
