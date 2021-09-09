package de.robolab.client.repl

import de.robolab.client.repl.base.ReplCommandParameterDescriptor
import de.robolab.client.repl.base.IReplCommandLeaf

open class ReplSimpleCommand(
    override val name: String,
    override val description: String,
    private val executeHandler: suspend () -> List<String>,
): IReplCommandLeaf {
    override val parameters: List<ReplCommandParameterDescriptor<*>> = emptyList()

    override suspend fun execute(parameters: List<String>): List<String> {
        if (parameters.isNotEmpty()) {
            throw IllegalArgumentException("No parameters were expected, got ${parameters.size}!")
        }

        return executeHandler()
    }
}
