package de.robolab.client.utils

import kotlin.reflect.KClass

class Flags<T>(val bits: Int) where T : Enum<T> {

    /*private data class EnumClass<T>(val clazz: KClass<T>, val values: List<T>) where T : Enum<T> {
        companion object {
            private val cache: MutableMap<KClass<*>, EnumClass<*>> = mutableMapOf()

            @Suppress("UNCHECKED_CAST")
            fun <T> getClass(clazz: KClass<T>): EnumClass<T> where T : Enum<T> {
                return cache.getOrPut(clazz) { reflectClass(clazz) } as EnumClass<T>
            }

            fun <T> getValues(clazz: KClass<T>): List<T> where T : Enum<T> = getClass(clazz).values

            private fun <T> reflectClass(clazz: KClass<T>): EnumClass<T> where T : Enum<T> {
                return EnumClass(
                    clazz, //reference "sealedSubclasses" cannot be resolved?
                    clazz.sealedSubclasses.mapNotNull { it.objectInstance}.sortedBy(Enum<T>::ordinal)
                )
            }
        }
    }*/

    val size: Int = bits.countOneBits()

    operator fun contains(element: T): Boolean {
        val bit = 1 shl element.ordinal
        return bit == (bits and bit)
    }

    fun containsAll(flags: Flags<T>): Boolean = flags.bits == (bits and flags.bits)

    fun containsAll(elements: Collection<T>): Boolean {
        val bits = elements.fold(0) { acc, it ->
            acc or (1 shl it.ordinal)
        }
        return bits == (this.bits and bits)
    }

    fun isEmpty(): Boolean = bits == 0

    /*override fun iterator(): Iterator<T> = object : Iterator<T> {
        var remainingBits = bits

        override fun hasNext(): Boolean = remainingBits != 0

        override fun next(): T {
            val workBits = remainingBits.takeLowestOneBit()
            if (workBits == 0) throw NoSuchElementException()
            remainingBits = remainingBits and workBits.inv()
            if (workBits == Int.MIN_VALUE)
                return clazz.values[Int.SIZE_BITS - 1]
            return clazz.values[log2(workBits)]
        }
    }*/

    inline fun <reified T2> toSet(): Set<T2> where T2 : T {
        return enumValues<T2>().filterTo(mutableSetOf(), this::contains)
    }

    inline fun <reified T2> toList(): List<T2> where T2 : T {
        return enumValues<T2>().filter(this::contains)
    }

    operator fun plus(value: T): Flags<T> = Flags(bits or (1 shl value.ordinal))

    operator fun plus(values: Flags<T>): Flags<T> = Flags(bits or values.bits)

    operator fun minus(value: T): Flags<T> = Flags(bits and (1 shl value.ordinal).inv())

    operator fun minus(values: Flags<T>): Flags<T> = Flags(bits and values.bits)

    fun flip(value: T): Flags<T> = Flags(bits xor (1 shl value.ordinal))

    fun flip(values: Flags<T>): Flags<T> = Flags(bits xor values.bits)
}

fun <T> Int.toFlags(): Flags<T> where T : Enum<T> = Flags(this)

fun <T> Collection<T>.toFlags(): Flags<T> where T : Enum<T> {
    return fold(0) { acc, it ->
        acc or (1 shl it.ordinal)
    }.toFlags()
}

fun <T> emptyFlags(): Flags<T> where T : Enum<T> = Flags(0)

fun <T> flagsOf(vararg elements: T): Flags<T> where T : Enum<T> {
    return elements.fold(0) { acc, it ->
        acc or (1 shl it.ordinal)
    }.toFlags()
}
