package de.robolab.common.planet

import de.robolab.common.planet.utils.IPlanetValue
import de.robolab.common.utils.Vector
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.math.absoluteValue

@Serializable(with = PlanetDirection.Companion::class)
enum class PlanetDirection(val value: String) : IPlanetValue<PlanetDirection> {
    East("EAST"),
    North("NORTH"),
    South("SOUTH"),
    West("WEST");

    val letter: Char
        get() = name.first()

    fun opposite() = when (this) {
        North -> South
        East -> West
        South -> North
        West -> East
    }

    fun turnClockwise() = when (this) {
        North -> East
        East -> South
        South -> West
        West -> North
    }

    fun turnCounterClockwise() = when (this) {
        North -> West
        East -> North
        South -> East
        West -> South
    }

    fun toVector(size: Double = 1.0) = when (this) {
        North -> Vector(0.0, size)
        East -> Vector(size, 0.0)
        South -> Vector(0.0, -size)
        West -> Vector(-size, 0.0)
    }

    fun rotate(direction: Planet.RotateDirection) = when (direction) {
        Planet.RotateDirection.CLOCKWISE -> turnClockwise()
        Planet.RotateDirection.COUNTER_CLOCKWISE -> turnCounterClockwise()
    }

    companion object : KSerializer<PlanetDirection> {
        override val descriptor: SerialDescriptor
            get() {
                return PrimitiveSerialDescriptor("de.robolab.common.planet.PlanetDirection", PrimitiveKind.STRING)
            }

        override fun deserialize(decoder: Decoder): PlanetDirection = when (val value = decoder.decodeString()) {
            "EAST" -> East
            "NORTH" -> North
            "SOUTH" -> South
            "WEST" -> West
            else -> throw IllegalArgumentException("PlanetDirection could not parse: $value")
        }

        override fun serialize(encoder: Encoder, value: PlanetDirection) {
            return encoder.encodeString(value.value)
        }

        fun fromVector(vector: Vector): PlanetDirection {
            return if (vector.x.absoluteValue < vector.y.absoluteValue) {
                if (vector.y >= 0) North else South
            } else {
                if (vector.x >= 0) East else West
            }
        }

        fun fromLetter(letter: Char): PlanetDirection? = when (letter.uppercaseChar()) {
            'N' -> North
            'E' -> East
            'S' -> South
            'W' -> West
            else -> null
        }
    }
}
