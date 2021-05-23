package de.robolab.client.communication

import de.robolab.client.utils.flagsOf
import de.robolab.common.net.data.OdometryData
import de.robolab.common.net.data.OdometryPayloadFlags
import de.robolab.common.utils.decodeBytesFromB64
import io.ktor.utils.io.core.*

data class StatsMessage(
    val type: Type,
    val wasB64: Boolean
) {
    companion object {
        private val b64Chars: Set<Char> = (listOf(
            '+',
            '/',
            '=',
        ) + listOf(
            '0'..'9',
            'A'..'Z',
            'a'..'z',
        ).flatMap(CharRange::asIterable)).toSet()

        fun decode(message: String): Pair<StatsMessage, ByteReadPacket> {
            val b64: Boolean = message.first() in b64Chars
            val packet = ByteReadPacket(
                if (!b64) message.toByteArray()
                else message.decodeBytesFromB64()
            )
            val typeByte = packet.readByte()
            return StatsMessage(
                Type.forID(typeByte)
                    ?: throw IllegalArgumentException("Could not find StatsMessage-Type for id $typeByte"),
                b64
            ) to packet
        }
    }

    enum class Type(val id: Byte) {
        RAW_XYA(0x01) {
            override fun parseMessage(
                metadata: RobolabMessage.Metadata,
                message: StatsMessage,
                readPacket: ByteReadPacket
            ): RobolabMessage {
                readPacket.discardExact(2)
                val formatChar = readPacket.readByte().toInt().toChar()
                val entryCount: Int = readPacket.readInt()
                //println(">>> FC:'$formatChar'; EC:$entryCount; R:${readPacket.remaining}")
                //println(">>> Bytes:")
                //println(hex(readPacket.copy().readBytes()).chunked(8) { "$it" }.joinToString(separator = " "))
                val odoData = OdometryData(
                    when (formatChar) {
                        'f' -> {
                            val array = FloatArray(3 * entryCount)
                            readPacket.readFully(array, 0, 3 * entryCount)
                            //println(">>> F-Array:")
                            //println(array)
                            array.toList().chunked(3) { (x, y, a) -> OdometryData.PositionXYA(x, y, a) }.toTypedArray()
                        }
                        'd' -> {
                            val array = DoubleArray(3 * entryCount)
                            readPacket.readFully(array, 0, 3 * entryCount)
                            //println(">>> D-Array:")
                            //println(array)
                            array.toList().chunked(3) { (x, y, a) ->
                                OdometryData.PositionXYA(x.toFloat(), y.toFloat(), a.toFloat())
                            }.toTypedArray()
                        }
                        else -> throw IllegalArgumentException("Could not deconstruct stats-message with format char '$formatChar'")
                    }
                )
                //println(">|> R: ${readPacket.remaining}")
                return RobolabMessage.OdometryMessage(
                    metadata,
                    odoData,
                    payloadFlags = flagsOf(
                        OdometryPayloadFlags.BASE64,
                        OdometryPayloadFlags.ANGLE_DEGREES,
                        OdometryPayloadFlags.POSITION_GRID_UNITS,
                    )
                )
            }
        }
        ;

        companion object {
            private val types: List<Type> = values().sortedBy(Type::id)
            fun forID(id: Byte): Type? {
                val index = types.binarySearchBy(id, selector = Type::id)
                return if (index < 0) null
                else types[index]
            }
        }

        abstract fun parseMessage(
            metadata: RobolabMessage.Metadata,
            message: StatsMessage,
            readPacket: ByteReadPacket
        ): RobolabMessage
    }
}