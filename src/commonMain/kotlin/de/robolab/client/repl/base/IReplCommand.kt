package de.robolab.client.repl.base

import kotlin.reflect.KClass

interface IReplCommand {

    val name: String

    val description: String

    fun printHelp(parentNames: List<String>): List<String>
}

interface IReplCommandNode : IReplCommand {

    val commands: List<IReplCommand>

    override fun printHelp(parentNames: List<String>): List<String> {
        return buildList {
            add((parentNames + name).joinToString(" "))
            add("")
            add("Description:")
            add("    $description")
            add("")
            add("Commands:")
            for (command in commands) {
                add("    ${command.name}: ${command.description}")
            }
        }
    }
}

interface IReplCommandLeaf : IReplCommand {

    val parameters: List<ReplCommandParameterDescriptor<*>>

    suspend fun execute(parameters: List<String>): List<String>

    override fun printHelp(parentNames: List<String>): List<String> {
        val params = parameters.joinToString(" ") {
            if (it.optional) {
                "[optional ${it.name} ${it.type.name} \"${it.type.example}\"]"
            } else {
                "[${it.name} ${it.type.name} \"${it.type.example}\"]"
            }
        }

        return buildList {
            add((parentNames + name).joinToString(" "))
            add("")
            add("Description:")
            add("    $description")
            add("")
            add("Usage:")
            add("    $name: $params")
        }
    }
}

interface IReplCommandParameter {
    val typeDescriptor: IReplCommandParameterTypeDescriptor<*>
    fun toToken(): String
}

interface IReplCommandParameterTypeDescriptor<T> where T : IReplCommandParameter {

    val klazz: KClass<T>

    val name: String
    val example: String
    val regex: Regex

    fun fromToken(token: String): T?
}


interface IReplCommandPicker<T> where T : IReplCommandParameter {
    val klazz: KClass<T>

    suspend fun pick(): T
}

data class ReplCommandParameterDescriptor<T>(
    val type: IReplCommandParameterTypeDescriptor<T>,
    val name: String,
    val optional: Boolean = false,
) where T : IReplCommandParameter

fun<T> buildList(builder: MutableList<T>.() -> Unit): List<T> {
    val list = mutableListOf<T>()
    builder(list)
    return list.toList()
}
