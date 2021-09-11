package de.robolab.client.repl.base

import kotlin.reflect.KClass

interface IReplCommandParameter {
    val typeDescriptor: IReplCommandParameterTypeDescriptor<*>
    fun toToken(): String
}

interface IReplCommandParameterTypeDescriptor<T> where T : IReplCommandParameter {

    val klazz: KClass<T>

    val name: String
    val description: String
    val example: List<String>
    val pattern: String
    val regex: Regex

    fun fromToken(token: String): T?
}

data class ReplCommandParameterDescriptor<T>(
    val type: IReplCommandParameterTypeDescriptor<T>,
    val name: String,
    val optional: Boolean = false,
) where T : IReplCommandParameter

inline fun<reified T1: IReplCommandParameter?> List<IReplCommandParameter>.parse1(): T1 {
    return this[0] as T1
}

inline fun<reified T1: IReplCommandParameter?, reified T2: IReplCommandParameter?> List<IReplCommandParameter>.parse2(): Pair<T1, T2> {
    return Pair(this[0] as T1, this[1] as T2)
}

inline fun<reified T1: IReplCommandParameter?, reified T2: IReplCommandParameter?, reified T3: IReplCommandParameter?> List<IReplCommandParameter>.parse3(): Triple<T1, T2, T3> {
    return Triple(this[0] as T1, this[1] as T2, this[2] as T3)
}
