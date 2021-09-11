package de.robolab.client.repl

import de.robolab.client.repl.base.IReplCommandParameter
import de.robolab.client.repl.base.IReplCommandParameterTypeDescriptor
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
    executeHandler: suspend () -> List<String>,
): ReplSimpleCommand {
    val c = ReplSimpleCommand(name, description, executeHandler)
    addCommand(c)
    return c
}

fun ReplCommandNode.actionNoOutput(
    name: String,
    description: String,
    executeHandler: suspend () -> Unit,
): ReplSimpleCommand {
    val c = ReplSimpleCommand(name, description) {
        executeHandler()
        emptyList()
    }
    addCommand(c)
    return c
}

fun ReplCommandNode.action(
    name: String,
    description: String,
    vararg parameters: ReplCommandParameterDescriptor<*>,
    executeHandler: suspend (params: List<IReplCommandParameter>) -> List<String>,
): ReplParameterCommand {
    val c = ReplParameterCommand(name, description, *parameters, executeHandler = executeHandler)
    addCommand(c)
    return c
}

fun ReplCommandNode.actionNoOutput(
    name: String,
    description: String,
    vararg parameters: ReplCommandParameterDescriptor<*>,
    executeHandler: suspend (params: List<IReplCommandParameter>) -> Unit,
): ReplParameterCommand {
    val c = ReplParameterCommand(name, description, *parameters) {
        executeHandler(it)
        emptyList()
    }
    addCommand(c)
    return c
}

fun <T : IReplCommandParameter> IReplCommandParameterTypeDescriptor<T>.param(
    name: String,
    optional: Boolean = false,
): ReplCommandParameterDescriptor<T> {
    return ReplCommandParameterDescriptor(this, name, optional)
}
