package de.robolab.client.repl

import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.repl.base.*


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

object DummyReplOutput : IReplOutput {
    override fun writeString(message: String, color: ReplColor?) {
    }
    override fun writeIcon(icon: MaterialIcon, color: ReplColor?) {
    }
}

fun ReplExecutor.HintColor.toColor() = when (this) {
    ReplExecutor.HintColor.NODE -> ReplColor.BLUE
    ReplExecutor.HintColor.LEAF -> ReplColor.CYAN
    ReplExecutor.HintColor.PARAMETER -> ReplColor.GREEN
    ReplExecutor.HintColor.ERROR -> ReplColor.RED
}
