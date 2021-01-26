package de.robolab.common.planet

sealed class TestModifier {

    abstract fun translate(delta: de.robolab.common.planet.Coordinate): TestModifier
    abstract fun rotate(direction: Planet.RotateDirection, origin: de.robolab.common.planet.Coordinate): TestModifier

    data class Coordinate(
        val type: Type,
        val coordinate: de.robolab.common.planet.Coordinate,
        val signal: TestSignal?
    ) : TestModifier() {
        override fun translate(delta: de.robolab.common.planet.Coordinate): TestModifier {
            return Coordinate(type, coordinate.translate(delta), signal)
        }

        override fun rotate(
            direction: Planet.RotateDirection,
            origin: de.robolab.common.planet.Coordinate
        ): TestModifier {
            return Coordinate(type, coordinate.rotate(direction, origin), signal)
        }
    }

    data class Path(
        val type: Type,
        val coordinate: de.robolab.common.planet.Coordinate,
        val direction: Direction,
        val signal: TestSignal?
    ) : TestModifier() {
        override fun translate(delta: de.robolab.common.planet.Coordinate): TestModifier {
            return Path(type, coordinate.translate(delta), direction, signal)
        }

        override fun rotate(
            direction: Planet.RotateDirection,
            origin: de.robolab.common.planet.Coordinate
        ): TestModifier {
            return Path(type, coordinate.rotate(direction, origin), this.direction.rotate(direction), signal)
        }
    }

    enum class Type {
        ALLOW, DISALLOW, SKIP, UNSKIP
    }
}

fun TestModifier.serializeType(): String {
    val type = when (this) {
        is TestModifier.Coordinate -> type
        is TestModifier.Path -> type
    }

    return type.name.toLowerCase()
}