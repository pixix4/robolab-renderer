@file:Suppress("NOTHING_TO_INLINE")

package de.robolab.server.externaljs

import de.robolab.server.jsutils.isUndefined

typealias NodeError = Error

external interface JSArray<T> {
    val length: Int

    fun concat(other: JSArray<T>): JSArray<T>
    fun copyWithin(target: Int): JSArray<T>
    fun copyWithin(target: Int, start: Int): JSArray<T>
    fun copyWithin(target: Int, start: Int, end: Int): JSArray<T>
    fun every(callback: (element: T) -> Boolean): Boolean
    fun every(callback: (element: T, index: Int) -> Boolean): Boolean
    fun every(callback: (element: T, index: Int, array: JSArray<T>) -> Boolean): Boolean
    fun fill(value: T): JSArray<T>
    fun fill(value: T, start: Int): JSArray<T>
    fun fill(value: T, start: Int, end: Int): JSArray<T>
    fun filter(callback: (element: T) -> Boolean): JSArray<T>
    fun filter(callback: (element: T, index: Int) -> Boolean): JSArray<T>
    fun filter(callback: (element: T, index: Int, array: JSArray<T>) -> Boolean): JSArray<T>
    fun find(callback: (element: T) -> Boolean): T?
    fun find(callback: (element: T, index: Int) -> Boolean): T?
    fun find(callback: (element: T, index: Int, array: JSArray<T>) -> Boolean): T?
    fun findIndex(callback: (element: T) -> Boolean): Int
    fun findIndex(callback: (element: T, index: Int) -> Boolean): Int
    fun findIndex(callback: (element: T, index: Int, array: JSArray<T>) -> Boolean): Int
    fun <F> flatMap(callback: (currentValue: T) -> JSArray<F>): JSArray<F>
    fun <F> flatMap(callback: (currentValue: T, index: Int) -> JSArray<F>): JSArray<F>
    fun <F> flatMap(callback: (currentValue: T, index: Int, array: JSArray<T>) -> JSArray<F>): JSArray<F>
    fun forEach(callback: (element: T) -> Boolean)
    fun forEach(callback: (element: T, index: Int) -> Boolean)
    fun forEach(callback: (element: T, index: Int, array: JSArray<T>) -> Boolean)
    fun includes(valueToFind: T): Boolean
    fun includes(valueToFind: T, fromIndex: Int): Boolean
    fun indexOf(searchElement: T): Int
    fun indexOf(searchElement: T, fromIndex: Int): Int
    fun join(): String
    fun join(separator: String): String
    fun lastIndexOf(searchElement: T): Int
    fun lastIndexOf(searchElement: T, fromIndex: Int): Int
    fun <F> flatMap(callback: (currentValue: T) -> F): JSArray<F>
    fun <F> flatMap(callback: (currentValue: T, index: Int) -> F): JSArray<F>
    fun <F> flatMap(callback: (currentValue: T, index: Int, array: JSArray<T>) -> F): JSArray<F>
    fun pop(): T?
    fun push(element1: T, vararg elementN: T): Int
    fun <F> reduce(callback: (accumulator: F, currentValue: T) -> F): F
    fun <F> reduce(callback: (accumulator: F, currentValue: T, index: Int) -> F): F
    fun <F> reduce(callback: (accumulator: F, currentValue: T, index: Int, array: JSArray<T>) -> F): F
    fun <F> reduce(callback: (accumulator: F, currentValue: T) -> F, initialValue: F): F
    fun <F> reduce(callback: (accumulator: F, currentValue: T, index: Int) -> F, initialValue: F): F
    fun <F> reduce(callback: (accumulator: F, currentValue: T, index: Int, array: JSArray<T>) -> F, initialValue: F): F
    fun <F> reduceRight(callback: (accumulator: F, currentValue: T) -> F): F
    fun <F> reduceRight(callback: (accumulator: F, currentValue: T, index: Int) -> F): F
    fun <F> reduceRight(callback: (accumulator: F, currentValue: T, index: Int, array: JSArray<T>) -> F): F
    fun <F> reduceRight(callback: (accumulator: F, currentValue: T) -> F, initialValue: F): F
    fun <F> reduceRight(callback: (accumulator: F, currentValue: T, index: Int) -> F, initialValue: F): F
    fun <F> reduceRight(
        callback: (accumulator: F, currentValue: T, index: Int, array: JSArray<T>) -> F,
        initialValue: F
    ): F

    fun reverse(): JSArray<T>
    fun shift(): T?
    fun slice(): JSArray<T>
    fun slice(begin: Int): JSArray<T>
    fun slice(begin: Int, end: Int): JSArray<T>
    fun some(callback: (element: T) -> Boolean): Boolean
    fun some(callback: (element: T, index: Int) -> Boolean): Boolean
    fun some(callback: (element: T, index: Int, array: JSArray<T>) -> Boolean): Boolean
    fun sort(): JSArray<T>
    fun sort(compareFunction: (firstEl: T, secondEl: T) -> Int): JSArray<T>
    fun splice(start: Int): JSArray<T>
    fun splice(start: Int, deleteCount: Int): JSArray<T>
    fun splice(start: Int, deleteCount: Int, item1: T): JSArray<T>
    fun splice(start: Int, deleteCount: Int, item1: T, item2: T): JSArray<T>
    fun splice(start: Int, deleteCount: Int, item1: T, item2: T, vararg itemN: T): JSArray<T>
    fun toLocaleString(): String
    fun toLocaleString(locales: String): String
    fun toLocaleString(locales: String, options: dynamic): String
    fun unshift(element1: T, vararg elementN: T): Int
    fun values(): Iterator<T>
}

external interface JSObject<K, T> {
    operator fun get(key: K): T
}

typealias DynJSArray = JSArray<dynamic>

fun <T> JSArray<T>.toList(): List<T> {
    return this.toMutableList().toList()
}

fun <T> JSArray<T>.toMutableList(): MutableList<T> {
    val lst = mutableListOf<T>()
    this.forEach { element ->
        lst.add(element)
    }
    return lst
}

fun <T> List<T>.toJSArray(): JSArray<T> {
    val arr = emptyJSArray<T>()
    for (element: T in this)
        arr.push(element)
    return arr
}

inline fun <T> emptyJSArray(): JSArray<T> {
    return js("[]").unsafeCast<JSArray<T>>()
}

inline fun emptyJSArray(): DynJSArray {
    return js("[]").unsafeCast<DynJSArray>()
}

inline fun emptyDynamic(): dynamic {
    return js("{}")
}

inline fun dynamicAlso(block: (dynamic) -> Unit): dynamic {
    val dyn = emptyDynamic()
    block(dyn)
    return dyn
}

fun dynamicOf(vararg entries: Pair<String, *>): dynamic {
    val dyn = emptyDynamic()
    for ((key, value) in entries)
        dyn[key] = value
    return dyn
}

fun dynamicOf(entries: Iterable<Pair<String, *>>): dynamic {
    val dyn = emptyDynamic()
    for ((key, value) in entries)
        dyn[key] = value
    return dyn
}

fun dynamicOfDefined(vararg entries: Pair<String, *>): dynamic {
    val dyn = emptyDynamic()
    for ((key, value) in entries)
        if (!value.isUndefined())
            dyn[key] = value
    return dyn
}

fun dynamicOfDefined(entries: Iterable<Pair<String, *>>): dynamic {
    val dyn = emptyDynamic()
    for ((key, value) in entries)
        if (!value.isUndefined())
            dyn[key] = value
    return dyn
}

inline fun <K, T> emptyJSObject(): JSObject<K, T> {
    return emptyDynamic().unsafeCast<JSObject<K, T>>()
}

private val jsIsArray: (Any?) -> Boolean = js("Array.isArray") as (Any?) -> Boolean

fun isJSArray(obj: Any?): Boolean = jsIsArray(obj)

fun <T> jsArrayOf(vararg elements: T): JSArray<T> {
    val arr = emptyJSArray<T>()
    for (element: T in elements)
        arr.push(element)
    return arr
}

fun jsArrayOf(vararg elements: dynamic): DynJSArray {
    val arr = emptyJSArray()
    for (element: dynamic in elements)
        arr.push(element)
    return arr
}


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