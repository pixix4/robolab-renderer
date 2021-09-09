package de.robolab.client.repl

import de.robolab.client.repl.base.IReplCommand
import de.robolab.client.repl.base.IReplCommandNode

open class ReplCommandNode(
    override val name: String,
    override val description: String,
): IReplCommandNode {

    private val mutableCommands = mutableListOf<IReplCommand>()

    override val commands: List<IReplCommand>
        get() = mutableCommands

    fun addCommand(command: IReplCommand) {
        mutableCommands += command
    }
    operator fun plusAssign(command: IReplCommand) = addCommand(command)

    fun removeCommand(command: IReplCommand) {
        mutableCommands -= command
    }
    operator fun minusAssign(command: IReplCommand) = removeCommand(command)
}
