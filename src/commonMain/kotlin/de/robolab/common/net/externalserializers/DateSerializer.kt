package de.robolab.common.net.externalserializers

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.parseUtc
import kotlinx.serialization.*


@Serializer(forClass = DateTime::class)
object DateSerializer : KSerializer<DateTime> {
    val format = DateFormat.FORMAT1

    override val descriptor: SerialDescriptor = PrimitiveDescriptor("DateTimeSerializer", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: DateTime) {
        encoder.encodeString(format.format(value.utc))
    }

    override fun deserialize(decoder: Decoder): DateTime {
        return format.parseUtc(decoder.decodeString())
    }
}