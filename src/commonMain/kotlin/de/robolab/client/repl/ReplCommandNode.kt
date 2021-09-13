package de.robolab.client.repl

import de.robolab.client.repl.base.IReplCommand
import de.robolab.client.repl.base.IReplCommandNode
import de.robolab.client.repl.base.ReplCommandParameterDescriptor

open class ReplCommandNode(
    override val name: String,
    override val description: String,
) : IReplCommandNode {

    constructor(name: String, description: String, init: ReplCommandNode.() -> Unit) : this(name, description) {
        init()
    }

    private val mutableCommands = mutableListOf<IReplCommand>()

    override val commands: List<IReplCommand>
        get() = mutableCommands

    fun addCommand(command: IReplCommand) {
        if (command in mutableCommands) return
        mutableCommands += command
    }

    operator fun plusAssign(command: IReplCommand) = addCommand(command)

    fun removeCommand(command: IReplCommand) {
        mutableCommands -= command
    }

    operator fun minusAssign(command: IReplCommand) = removeCommand(command)

    private var handler: suspend (type: ReplCommandParameterDescriptor<*>) -> List<ReplExecutor.AutoComplete>? = { null }
    fun setRequestAutoCompleteForHandler(handler: suspend (type: ReplCommandParameterDescriptor<*>) -> List<ReplExecutor.AutoComplete>?) {
        this.handler = handler
    }

    override suspend fun requestAutoCompleteFor(type: ReplCommandParameterDescriptor<*>): List<ReplExecutor.AutoComplete>? {
        return handler(type)
    }
}
