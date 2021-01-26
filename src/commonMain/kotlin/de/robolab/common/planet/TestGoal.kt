package de.robolab.common.planet

sealed class TestGoal {

    abstract fun translate(delta: Coordinate): TestGoal
    abstract fun rotate(direction: Planet.RotateDirection, origin: Coordinate): TestGoal

    data class Target(
        val coordinate: Coordinate
    ): TestGoal() {
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

    object Explore: TestGoal() {
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
        val coordinate: Coordinate
    ): TestGoal() {
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
