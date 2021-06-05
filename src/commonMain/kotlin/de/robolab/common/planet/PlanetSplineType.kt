package de.robolab.common.planet

import de.robolab.common.utils.Vector
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = PlanetSplineType.Companion::class)
enum class PlanetSplineType(val value: String) {
    BSpline("B-SPLINE") {
        override fun length(
            sourceX: Long,
            sourceY: Long,
            targetX: Long,
            targetY: Long,
            controlPoints: List<PlanetCoordinate>
        ): Double {
            return (listOf(Vector(sourceX, sourceY)) + controlPoints.map { it.point } + Vector(
                targetX,
                targetY
            )).windowed(2, 1)
                .sumOf { (p0, p1) -> p0.distanceTo(p1) }
        }
    };

    abstract fun length(
        sourceX: Long,
        sourceY: Long,
        targetX: Long,
        targetY: Long,
        controlPoints: List<PlanetCoordinate>,
    ): Double

    companion object : KSerializer<PlanetSplineType> {
        override val descriptor: SerialDescriptor
            get() {
                return PrimitiveSerialDescriptor(
                    "de.robolab.common.planet.PlanetSplineType",
                    PrimitiveKind.STRING
                )
            }

        override fun deserialize(decoder: Decoder): PlanetSplineType = when (val value = decoder.decodeString()) {
            "B-SPLINE" -> BSpline
            else -> throw IllegalArgumentException("PlanetSplineType could not parse: $value")
        }

        override fun serialize(encoder: Encoder, value: PlanetSplineType) {
            return encoder.encodeString(value.value)
        }
    }
}
