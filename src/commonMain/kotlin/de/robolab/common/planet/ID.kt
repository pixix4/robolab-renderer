package de.robolab.common.planet

import de.robolab.client.traverser.nextHexString
import kotlinx.serialization.*
import kotlin.random.Random

class ID(val id: String) : IPlanetValue {
    override fun toString(): String = id
}

fun randomName(): String = "Planet-${Random.nextHexString(3)}-${Random.nextHexString(5)}"

object IDSerializer : KSerializer<ID> {
    override val descriptor: SerialDescriptor = SerialDescriptor("IDSerializer", kind = PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ID = ID(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: ID) = encoder.encodeString(value.id)

}