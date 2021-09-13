package de.robolab.client.repl

import de.robolab.client.repl.base.IReplBoundCommandTemplate
import de.robolab.client.repl.base.IReplCommandNode
import de.robolab.client.repl.base.ReplCommandParameterDescriptor

abstract class ReplBoundCommandNodeTemplate<T>(
    val name: String,
    val description: String,
) : IReplBoundCommandTemplate<T> {

    abstract val children: List<IReplBoundCommandTemplate<T>>

    override fun bind(ref: T): IReplCommandNode {
        val node = ReplCommandNode(name, description)
        children.map { it.bind(ref) }.forEach(node::addCommand)

        node.setRequestAutoCompleteForHandler {
            ref.requestAutoCompleteFor(it)
        }

        return node
    }

    open suspend fun T.requestAutoCompleteFor(type: ReplCommandParameterDescriptor<*>): List<ReplExecutor.AutoComplete>? {
        return null
    }

    companion object {
        operator fun <T> invoke(name: String, description: String, children: List<IReplBoundCommandTemplate<T>>) =
            object : ReplBoundCommandNodeTemplate<T>(name, description) {
                override val children: List<IReplBoundCommandTemplate<T>>
                    get() = children
            }
    }
}
