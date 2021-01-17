package de.robolab.common.planet

import de.robolab.client.traverser.nextHexString
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.random.Random

class ID(val id: String) : IPlanetValue {
    override fun toString(): String = id

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ID) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

fun randomName(): String = "Planet-${Random.nextHexString(3)}-${Random.nextHexString(5)}"

object IDSerializer : KSerializer<ID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("IDSerializer", kind = PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ID = ID(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: ID) = encoder.encodeString(value.id)

}