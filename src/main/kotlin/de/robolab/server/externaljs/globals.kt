package de.robolab.server.externaljs

external class Buffer {
    companion object {
        fun alloc(size: Int): Buffer
        fun alloc(size: Int, fill: Int): Buffer
        fun alloc(size: Int, fill: Buffer): Buffer
        fun alloc(size: Int, fill: String): Buffer
        fun alloc(size: Int, fill: String, encoding: String): Buffer

        fun byteLength(string: String): Int
        fun byteLength(string: String, encoding: String): Int

        fun compare(buf1: Buffer, buf2: Buffer): Int

        fun concat(list: List<Buffer>): Buffer
        fun concat(list: List<Buffer>, totalLength: Int): Buffer

        fun from(array: IntArray): Buffer
        fun from(buffer: Buffer): Buffer
        fun from(string: String): Buffer
        fun from(string: String, encoding: String): Buffer

        fun isBuffer(obj: Any?): Boolean

        fun isEncoding(encoding: String): Boolean

        fun transcode(source: Buffer, fromEnc: String, toEnc: String): Buffer
    }

    fun get(index: Int): Int?
    fun set(index: Int, value: Int)

    fun compare(target: Buffer): Int
    fun compare(target: Buffer, targetStart: Int): Int
    fun compare(target: Buffer, targetStart: Int, targetEnd: Int): Int
    fun compare(target: Buffer, targetStart: Int, targetEnd: Int, sourceStart: Int): Int
    fun compare(target: Buffer, targetStart: Int, targetEnd: Int, sourceStart: Int, sourceEnd: Int): Int

    fun copy(target: Buffer): Int
    fun copy(target: Buffer, targetStart: Int): Int
    fun copy(target: Buffer, targetStart: Int, sourceStart: Int): Int
    fun copy(target: Buffer, targetStart: Int, sourceStart: Int, sourceEnd: Int): Int

    fun equals(otherBuffer: Buffer): Boolean

    fun fill(value: String): Buffer
    fun fill(value: String, offset: Int): Buffer
    fun fill(value: String, offset: Int, end: Int): Buffer
    fun fill(value: String, offset: Int, end: Int, encoding: String): Buffer
    fun fill(value: Int): Buffer
    fun fill(value: Int, offset: Int): Buffer
    fun fill(value: Int, offset: Int, end: Int): Buffer
    fun fill(value: Buffer): Buffer
    fun fill(value: Buffer, offset: Int): Buffer
    fun fill(value: Buffer, offset: Int, end: Int): Buffer

    fun includes(value: String): Boolean
    fun includes(value: String, byteOffset: Int): Boolean
    fun includes(value: String, byteOffset: Int, encoding: String): Boolean
    fun includes(value: Int): Boolean
    fun includes(value: Int, byteOffset: Int): Boolean
    fun includes(value: Buffer): Boolean
    fun includes(value: Buffer, byteOffset: Int): Boolean

    fun indexOf(value: String): Int
    fun indexOf(value: String, byteOffset: Int): Int
    fun indexOf(value: String, byteOffset: Int, encoding: String): Int
    fun indexOf(value: Int): Int
    fun indexOf(value: Int, byteOffset: Int): Int
    fun indexOf(value: Buffer): Int
    fun indexOf(value: Buffer, byteOffset: Int): Int

    fun lastIndexOf(value: String): Int
    fun lastIndexOf(value: String, byteOffset: Int): Int
    fun lastIndexOf(value: String, byteOffset: Int, encoding: String): Int
    fun lastIndexOf(value: Int): Int
    fun lastIndexOf(value: Int, byteOffset: Int): Int
    fun lastIndexOf(value: Buffer): Int
    fun lastIndexOf(value: Buffer, byteOffset: Int): Int

    val length: Int

    fun readBigInt64BE(): Long
    fun readBigInt64BE(offset: Int): Long
    fun readBigInt64LE(): Long
    fun readBigInt64LE(offset: Int): Long

    fun readDoubleBE(): Double
    fun readDoubleBE(offset: Int): Double
    fun readDoubleLE(): Double
    fun readDoubleLE(offset: Int): Double

    fun readFloatBE(): Float
    fun readFloatBE(offset: Int): Float
    fun readFloatLE(): Float
    fun readFloatLE(offset: Int): Float

    fun readInt8(): Byte
    fun readInt8(offset: Int): Byte

    fun readInt16BE(): Short
    fun readInt16BE(offset: Int): Short
    fun readInt16LE(): Short
    fun readInt16LE(offset: Int): Short

    fun readInt32BE(): Int
    fun readInt32BE(offset: Int): Int
    fun readInt32LE(): Int
    fun readInt32LE(offset: Int): Int

    fun readIntBE(offset: Int, byteLength: Int): Int
    fun readIntLE(offset: Int, byteLength: Int): Int

    fun subarray(): Buffer
    fun subarray(start: Int): Buffer
    fun subarray(start: Int, end: Int): Buffer

    fun slice(): Buffer
    fun slice(start: Int): Buffer
    fun slice(start: Int, end: Int): Buffer

    fun swap16(): Buffer
    fun swap32(): Buffer
    fun swap64(): Buffer

    fun toJSON(): Any

    override fun toString(): String
    fun toString(encoding: String): String
    fun toString(encoding: String, start: Int): String
    fun toString(encoding: String, start: Int, end: Int): String

    fun write(string: String): Int
    fun write(string: String, offset: Int): Int
    fun write(string: String, offset: Int, length: Int): Int
    fun write(string: String, offset: Int, length: Int, encoding: String): Int

    fun writeBigInt64BE(value: Long): Int
    fun writeBigInt64BE(value: Long, offset: Int): Int
    fun writeBigInt64LE(value: Long): Int
    fun writeBigInt64LE(value: Long, offset: Int): Int

    fun writeDoubleBE(value: Double): Int
    fun writeDoubleBE(value: Double, offset: Int): Int
    fun writeDoubleLE(value: Double): Int
    fun writeDoubleLE(value: Double, offset: Int): Int

    fun writeFloatBE(value: Float): Int
    fun writeFloatBE(value: Float, offset: Int): Int
    fun writeFloatLE(value: Float): Int
    fun writeFloatLE(value: Float, offset: Int): Int

    fun writeInt8(value: Byte): Int
    fun writeInt8(value: Byte, offset: Int): Int

    fun writeInt16BE(value: Short): Int
    fun writeInt16BE(value: Short, offset: Int): Int
    fun writeInt16LE(value: Short): Int
    fun writeInt16LE(value: Short, offset: Int): Int

    fun writeInt32BE(value: Int): Int
    fun writeInt32BE(value: Int, offset: Int): Int
    fun writeInt32LE(value: Int): Int
    fun writeInt32LE(value: Int, offset: Int): Int

    fun writeIntBE(value: Int, offset: Int, byteLength: Int): Int
    fun writeIntLE(value: Int, offset: Int, byteLength: Int): Int
}