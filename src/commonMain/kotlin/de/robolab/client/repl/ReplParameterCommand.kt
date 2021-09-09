package de.robolab.client.repl

import de.robolab.client.repl.base.ReplCommandParameterDescriptor
import de.robolab.client.repl.base.IReplCommandLeaf

open class ReplParameterCommand(
    override val name: String,
    override val description: String,
    vararg parameters: ReplCommandParameterDescriptor<*>,
    private val executeHandler: suspend (parameters: List<String>) -> List<String>,
): IReplCommandLeaf {
    override val parameters: List<ReplCommandParameterDescriptor<*>> = parameters.toList()

    override suspend fun execute(parameters: List<String>): List<String> {
        return executeHandler(parameters)
    }
}
