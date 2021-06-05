package de.robolab.common.planet

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = PlanetCommentAlignment.Companion::class)
enum class PlanetCommentAlignment(val value: String) {
    Center("CENTER"),
    Left("LEFT"),
    Right("RIGHT");

    companion object : KSerializer<PlanetCommentAlignment> {
        override val descriptor: SerialDescriptor
            get() {
                return PrimitiveSerialDescriptor("de.robolab.common.planet.PlanetCommentAlignment", PrimitiveKind.STRING)
            }

        override fun deserialize(decoder: Decoder): PlanetCommentAlignment = when (val value = decoder.decodeString()) {
            "CENTER" -> Center
            "LEFT" -> Left
            "RIGHT" -> Right
            else -> throw IllegalArgumentException("PlanetCommentAlignment could not parse: $value")
        }

        override fun serialize(encoder: Encoder, value: PlanetCommentAlignment) {
            return encoder.encodeString(value.value)
        }
    }
}
