package de.robolab.client.repl.base

import kotlin.reflect.KClass

interface IReplCommand {

    val name: String

    val description: String

    fun printHelp(parentNames: List<String>): List<String>
}

interface IReplCommandNode : IReplCommand {

    val commands: List<IReplCommand>

    val helpCommandDescriptions: List<Pair<String, String>>
        get() {
            val list = commands.map { it.name to it.description } + Pair("help", "Print help message for this command")
            return list.sortedBy { it.first }
        }

    override fun printHelp(parentNames: List<String>): List<String> {
        return buildList {
            add((parentNames + name).joinToString(" "))
            add("")
            add("Description:")
            add("    $description")
            add("")
            add("Commands:")

            val nameLength = helpCommandDescriptions.maxOf { it.first.length }
            for ((commandName, commandDescription) in helpCommandDescriptions) {
                val padding = " ".repeat(nameLength - commandName.length)
                add("    ${commandName}: $padding$commandDescription")
            }
        }
    }
}

interface IReplCommandLeaf : IReplCommand {

    val parameters: List<ReplCommandParameterDescriptor<*>>

    suspend fun execute(parameters: List<String>): List<String>

    override fun printHelp(parentNames: List<String>): List<String> {
        val params = parameters.joinToString(" ") {
            val s = "${it.name}: ${it.type.name}"
            if (it.optional) "[$s]" else "<$s>"
        }

        val types = parameters
            .asSequence()
            .map { it.type }
            .distinct()
            .sortedBy { it.name }
            .map {
                buildList<String> {
                    add("")
                    if (it.pattern.contains(' ')) {
                        add("${it.name}: \"${it.pattern}\"")
                    } else {
                        add("${it.name}: ${it.pattern}")
                    }
                    add("    ${it.description}")

                    if (it.example.isNotEmpty()) {
                        add("    Example: \"${it.example.first().escapeIfNecessary()}\"")
                    }
                    for (e in it.example.drop(1)) {
                        add("             \"${it.example.first().escapeIfNecessary()}\"")
                    }
                }
            }
            .flatten()
            .toList()

        return buildList {
            add((parentNames + name).joinToString(" "))
            add("")
            add("Description:")
            add("    $description")
            add("")
            add("Usage:")
            add("    $name $params")
            addAll(types)
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
    val description: String
    val example: List<String>
    val pattern: String
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


fun String.escapeIfNecessary(): String {
    val intern = if (this.contains('"')) this.replace("\"", "\\\"") else this
    return if (intern.contains(' ') || intern.contains('"')) "\"$intern\"" else intern
}
