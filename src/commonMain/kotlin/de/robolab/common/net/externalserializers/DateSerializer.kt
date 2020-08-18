package de.robolab.common.net.externalserializers

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.parseUtc
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


object DateSerializer : KSerializer<DateTime> {
    val format = DateFormat.FORMAT1

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DateTimeSerializer", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: DateTime) {
        encoder.encodeString(format.format(value.utc))
    }

    override fun deserialize(decoder: Decoder): DateTime {
        return format.parseUtc(decoder.decodeString())
    }
}