package de.robolab.common.testing

import de.robolab.common.planet.Coordinate
import de.robolab.common.planet.Planet

sealed class TestGoal {

    abstract fun translate(delta: Coordinate): TestGoal
    abstract fun rotate(direction: Planet.RotateDirection, origin: Coordinate): TestGoal

    abstract val coordinate: Coordinate?

    data class Target(
        override val coordinate: Coordinate
    ) : TestGoal() {
        override fun translate(delta: Coordinate): TestGoal {
            return Target(coordinate.translate(delta))
        }

        override fun rotate(direction: Planet.RotateDirection, origin: Coordinate): TestGoal {
            return Target(coordinate.rotate(direction, origin))
        }

        override fun toString(): String {
            return "Target ${coordinate.x}, ${coordinate.y}"
        }
    }

    object Explore : TestGoal() {
        override val coordinate: Coordinate? = null
        override fun translate(delta: Coordinate): TestGoal {
            return this
        }

        override fun rotate(direction: Planet.RotateDirection, origin: Coordinate): TestGoal {
            return this
        }

        override fun toString(): String {
            return "Explore"
        }
    }

    data class ExploreCoordinate(
        override val coordinate: Coordinate
    ) : TestGoal() {
        override fun translate(delta: Coordinate): TestGoal {
            return ExploreCoordinate(coordinate.translate(delta))
        }

        override fun rotate(direction: Planet.RotateDirection, origin: Coordinate): TestGoal {
            return ExploreCoordinate(coordinate.rotate(direction, origin))
        }

        override fun toString(): String {
            return "Explore ${coordinate.x}, ${coordinate.y}"
        }
    }
}
