package de.robolab.client.repl

import de.robolab.client.repl.base.IReplCommand
import de.robolab.client.repl.base.IReplCommandNode

class ReplCommandMergeNode(
    private val nodes: List<IReplCommandNode>,
) : IReplCommandNode {

    override val name = nodes.first().name

    override val description = nodes.find { it.description.isNotEmpty() }?.description ?: ""
    override val commands: List<IReplCommand>
        get() = nodes.flatMap { it.commands }
}

fun List<IReplCommand>.merge(): IReplCommand? {
    return when (size) {
        0 -> null
        1 -> first()
        else -> {
            ReplCommandMergeNode(mapNotNull { it as? IReplCommandNode })
        }
    }
}
