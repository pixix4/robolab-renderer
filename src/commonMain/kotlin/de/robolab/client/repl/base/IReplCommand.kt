package de.robolab.client.repl.base

import de.robolab.client.app.model.base.MaterialIcon
import kotlin.reflect.KClass

interface IReplCommand {

    val name: String

    val description: String

    fun printHelp(output: IReplOutput, parentNames: List<String>)
}

interface IReplCommandNode : IReplCommand {

    val commands: List<IReplCommand>

    val helpCommandDescriptions: List<Pair<String, String>>
        get() {
            val list = commands.map { it.name to it.description } + Pair("help", "Print help message for this command")
            return list.sortedBy { it.first }
        }

    override fun printHelp(output: IReplOutput, parentNames: List<String>) {
        output.writeln((parentNames + name).joinToString(" "))
        output.writeln()
        output.writeln("Description:")
        output.writeln("    $description")
        output.writeln()
        output.writeln("Commands:")

        val nameLength = helpCommandDescriptions.maxOf { it.first.length }
        for ((commandName, commandDescription) in helpCommandDescriptions) {
            val padding = " ".repeat(nameLength - commandName.length)
            output.writeln("    ${commandName}: $padding$commandDescription")
        }
    }
}

interface IReplCommandLeaf : IReplCommand {

    val parameters: List<ReplCommandParameterDescriptor<*>>

    suspend fun execute(output: IReplOutput, parameters: List<String>)

    override fun printHelp(output: IReplOutput, parentNames: List<String>) {
        val params = parameters.joinToString(" ") {
            val s = "${it.name}: ${it.type.name}"
            if (it.optional) "[$s]" else "<$s>"
        }

        output.writeln((parentNames + name).joinToString(" "))
        output.writeln("")
        output.writeln("Description:")
        output.writeln("    $description")
        output.writeln("")
        output.writeln("Usage:")
        output.writeln("    $name $params")

        for (parameter in parameters
            .asSequence()
            .map { it.type }
            .distinct()
            .sortedBy { it.name }) {
            output.writeln()
            if (parameter.pattern.contains(' ')) {
                output.writeln("${parameter.name}: \"${parameter.pattern}\"")
            } else {
                output.writeln("${parameter.name}: ${parameter.pattern}")
            }
            output.writeln("    ${parameter.description}")

            if (parameter.example.isNotEmpty()) {
                output.writeln("    Example: \"${parameter.example.first().escapeIfNecessary()}\"")
            }
            for (e in parameter.example.drop(1)) {
                output.writeln("             \"${parameter.example.first().escapeIfNecessary()}\"")
            }
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

data class ReplCommandParameterDescriptor<T>(
    val type: IReplCommandParameterTypeDescriptor<T>,
    val name: String,
    val optional: Boolean = false,
) where T : IReplCommandParameter

fun <T> buildList(builder: MutableList<T>.() -> Unit): List<T> {
    val list = mutableListOf<T>()
    builder(list)
    return list.toList()
}

fun String.escapeIfNecessary(): String {
    val intern = if (this.contains('"')) this.replace("\"", "\\\"") else this
    return if (intern.contains(' ') || intern.contains('"')) "\"$intern\"" else intern
}

enum class ReplColor {
    RED,
    GREEN,
    YELLOW,
    BLUE,
    MAGENTA,
    CYAN,
    GREY;
}

interface IReplOutput {

    fun writeString(message: String, color: ReplColor? = null)
    fun writeIcon(icon: MaterialIcon, color: ReplColor? = null)

    fun write(message: Any?, color: ReplColor? = null) = writeString(message.toString(), color)
    fun writeln(message: Any?, color: ReplColor? = null) = writeString("${message.toString()}\n", color)
    fun writeln() = writeString("\n", null)
}
