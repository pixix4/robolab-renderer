package de.robolab.common.testing

import de.robolab.common.planet.PlanetPoint
import de.robolab.common.planet.Planet

sealed class TestGoal {

    abstract fun translate(delta: PlanetPoint): TestGoal
    abstract fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint): TestGoal

    abstract val coordinate: PlanetPoint?
    abstract val type: GoalType

    data class Target(
        override val coordinate: PlanetPoint
    ) : TestGoal() {
        override val type: GoalType = GoalType.Target

        override fun translate(delta: PlanetPoint): TestGoal {
            return Target(coordinate.translate(delta))
        }

        override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint): TestGoal {
            return Target(coordinate.rotate(direction, origin))
        }

        override fun toString(): String {
            return "Target ${coordinate.x}, ${coordinate.y}"
        }
    }

    object Explore : TestGoal() {
        override val coordinate: PlanetPoint? = null
        override val type: GoalType = GoalType.Explore
        override fun translate(delta: PlanetPoint): TestGoal {
            return this
        }

        override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint): TestGoal {
            return this
        }

        override fun toString(): String {
            return "Explore"
        }
    }

    data class ExploreCoordinate(
        override val coordinate: PlanetPoint
    ) : TestGoal() {
        override val type: GoalType = GoalType.ExploreCoordinate
        override fun translate(delta: PlanetPoint): TestGoal {
            return ExploreCoordinate(coordinate.translate(delta))
        }

        override fun rotate(direction: Planet.RotateDirection, origin: PlanetPoint): TestGoal {
            return ExploreCoordinate(coordinate.rotate(direction, origin))
        }

        override fun toString(): String {
            return "Explore ${coordinate.x}, ${coordinate.y}"
        }
    }

    enum class GoalType {
        Target,
        Explore,
        ExploreCoordinate
    }
}
