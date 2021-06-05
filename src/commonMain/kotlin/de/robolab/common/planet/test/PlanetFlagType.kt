package de.robolab.common.planet.test

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = PlanetFlagType.Companion::class)
enum class PlanetFlagType(val value: String) {
    Disallow("DISALLOW"),
    Skip("SKIP");

    companion object : KSerializer<PlanetFlagType> {
        override val descriptor: SerialDescriptor
            get() {
                return PrimitiveSerialDescriptor("de.robolab.common.planet.test.PlanetFlagType", PrimitiveKind.STRING)
            }

        override fun deserialize(decoder: Decoder): PlanetFlagType = when (val value = decoder.decodeString()) {
            "DISALLOW" -> Disallow
            "SKIP" -> Skip
            else -> throw IllegalArgumentException("PlanetFlagType could not parse: $value")
        }

        override fun serialize(encoder: Encoder, value: PlanetFlagType) {
            return encoder.encodeString(value.value)
        }
    }
}
