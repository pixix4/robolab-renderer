package de.robolab.client.repl.base

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.repl.escapeIfNecessary
import de.robolab.client.repl.merge

interface IReplCommand {

    val name: String

    val description: String

    fun printHelp(output: IReplOutput, parentNames: List<String>)
}

interface IReplCommandNode : IReplCommand {

    val commands: List<IReplCommand>

    val helpCommandDescriptions: List<Pair<String, String>>
        get() {
            val list = commands.groupBy { it.name }.mapNotNull { it.value.merge() }
                .map { it.name to it.description } + Pair("help", "Print help message for this command")
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

interface IReplBoundCommandTemplate<in T> {
    fun bind(ref: T): IReplCommand
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
