package de.robolab.client.repl

import de.robolab.client.repl.base.ReplCommandParameterDescriptor
import de.robolab.client.repl.base.IReplCommandLeaf
import de.robolab.client.repl.base.IReplOutput

open class ReplSimpleCommand(
    override val name: String,
    override val description: String,
    private val executeHandler: suspend (output: IReplOutput) -> Unit,
): IReplCommandLeaf {
    override val parameters: List<ReplCommandParameterDescriptor<*>> = emptyList()

    override suspend fun execute(output: IReplOutput, parameters: List<String>) {
        if (parameters.isNotEmpty()) {
            throw IllegalArgumentException("No parameters were expected, got ${parameters.size}!")
        }

        return executeHandler(output)
    }
}
