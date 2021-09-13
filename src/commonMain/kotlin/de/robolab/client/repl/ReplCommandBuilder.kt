package de.robolab.client.repl

import de.robolab.client.repl.base.IReplCommandParameter
import de.robolab.client.repl.base.IReplCommandParameterTypeDescriptor
import de.robolab.client.repl.base.IReplOutput
import de.robolab.client.repl.base.ReplCommandParameterDescriptor


fun ReplCommandNode.node(
    name: String,
    description: String,
    builder: ReplCommandNode.() -> Unit,
): ReplCommandNode {
    val c = ReplCommandNode(name, description)
    addCommand(c)
    builder(c)
    return c
}

fun ReplCommandNode.action(
    name: String,
    description: String,
    executeHandler: suspend (output: IReplOutput) -> Unit,
): ReplSimpleCommand {
    val c = ReplSimpleCommand(name, description, executeHandler)
    addCommand(c)
    return c
}

fun ReplCommandNode.action(
    name: String,
    description: String,
    vararg parameters: ReplCommandParameterDescriptor<*>,
    executeHandler: suspend (output: IReplOutput, params: List<IReplCommandParameter>) -> Unit,
): ReplParameterCommand {
    val c = ReplParameterCommand(name, description, *parameters, executeHandler = executeHandler)
    addCommand(c)
    return c
}

fun <T : IReplCommandParameter> IReplCommandParameterTypeDescriptor<T>.param(
    name: String,
    optional: Boolean = false,
): ReplCommandParameterDescriptor<T> {
    return ReplCommandParameterDescriptor(this, name, optional)
}

fun <T> buildList(builder: MutableList<T>.() -> Unit): List<T> {
    val list = mutableListOf<T>()
    builder(list)
    return list.toList()
}

fun String.escapeIfNecessary(): String {
    val intern = if (this.contains('"')) this.replace("\"", "\\\"") else this
    return if (intern.contains(' ') || intern.contains('"')) "\"$intern\"" else intern
}
