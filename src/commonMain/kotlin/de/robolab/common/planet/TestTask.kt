package de.robolab.common.planet

sealed class TestTask {

    abstract fun translate(delta: de.robolab.common.planet.Coordinate): TestTask
    abstract fun rotate(direction: Planet.RotateDirection, origin: de.robolab.common.planet.Coordinate): TestTask

    data class Coordinate(
        val coordinate: de.robolab.common.planet.Coordinate,
        val signal: TestSignal?
    ) : TestTask() {
        override fun translate(delta: de.robolab.common.planet.Coordinate): TestTask {
            return Coordinate(coordinate.translate(delta), signal)
        }

        override fun rotate(direction: Planet.RotateDirection, origin: de.robolab.common.planet.Coordinate): TestTask {
            return Coordinate(coordinate.rotate(direction, origin), signal)
        }
    }

    data class Path(
        val coordinate: de.robolab.common.planet.Coordinate,
        val direction: Direction,
        val signal: TestSignal?
    ) : TestTask() {
        override fun translate(delta: de.robolab.common.planet.Coordinate): TestTask {
            return Path(coordinate.translate(delta), direction, signal)
        }

        override fun rotate(direction: Planet.RotateDirection, origin: de.robolab.common.planet.Coordinate): TestTask {
            return Path(coordinate.rotate(direction, origin), this.direction.rotate(direction), signal)
        }
    }
}