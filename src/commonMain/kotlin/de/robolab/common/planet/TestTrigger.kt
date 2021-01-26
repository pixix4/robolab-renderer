package de.robolab.common.planet

sealed class TestTrigger {

    abstract fun translate(delta: de.robolab.common.planet.Coordinate): TestTrigger
    abstract fun rotate(direction: Planet.RotateDirection, origin: de.robolab.common.planet.Coordinate): TestTrigger

    data class Coordinate(
        val coordinate: de.robolab.common.planet.Coordinate,
        val signal: TestSignal
    ) : TestTrigger() {
        override fun translate(delta: de.robolab.common.planet.Coordinate): TestTrigger {
            return Coordinate(coordinate.translate(delta), signal)
        }

        override fun rotate(
            direction: Planet.RotateDirection,
            origin: de.robolab.common.planet.Coordinate
        ): TestTrigger {
            return Coordinate(coordinate.rotate(direction, origin), signal)
        }
    }

    data class Path(
        val coordinate: de.robolab.common.planet.Coordinate,
        val direction: Direction,
        val signal: TestSignal
    ) : TestTrigger() {
        override fun translate(delta: de.robolab.common.planet.Coordinate): TestTrigger {
            return Path(coordinate.translate(delta), direction, signal)
        }

        override fun rotate(
            direction: Planet.RotateDirection,
            origin: de.robolab.common.planet.Coordinate
        ): TestTrigger {
            return Path(coordinate.rotate(direction, origin), this.direction.rotate(direction), signal)
        }
    }
}
