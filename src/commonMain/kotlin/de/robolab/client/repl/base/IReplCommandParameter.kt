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
