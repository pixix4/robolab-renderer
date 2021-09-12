package de.robolab.client.repl

import de.robolab.client.repl.base.IReplCommandLeaf
import de.robolab.client.repl.base.IReplCommandParameter
import de.robolab.client.repl.base.IReplOutput
import de.robolab.client.repl.base.ReplCommandParameterDescriptor

open class ReplParameterCommand(
    override val name: String,
    override val description: String,
    vararg parameters: ReplCommandParameterDescriptor<*>,
    private val executeHandler: suspend (output: IReplOutput, params: List<IReplCommandParameter>) -> Unit,
) : IReplCommandLeaf {
    override val parameters: List<ReplCommandParameterDescriptor<*>> = parameters.toList()

    override suspend fun execute(output: IReplOutput, parameters: List<String>) {
        val p = this.parameters.mapIndexedNotNull { i, p ->
            val nextString = parameters.getOrNull(i)
            if (nextString == null) {
                if (p.optional) {
                    null
                } else {
                    throw IllegalArgumentException("Required parameter '${p.name}' is missing!")
                }
            } else {
                val match = p.type.regex.matchEntire(nextString)
                if (match != null) {
                    Triple(p, nextString, match)
                } else {
                    throw IllegalArgumentException("Required parameter '${p.name}' does not match the given token '$nextString'!")
                }
            }
        }.mapNotNull { (type, param, match) ->
            type.type.fromToken(param, match)
        }

        return executeHandler(output, p)
    }

    private var handler: suspend (type: ReplCommandParameterDescriptor<*>) -> List<ReplExecutor.AutoComplete> = { emptyList() }
    fun setRequestAutoCompleteForHandler(handler: suspend (type: ReplCommandParameterDescriptor<*>) -> List<ReplExecutor.AutoComplete>) {
        this.handler = handler
    }

    override suspend fun requestAutoCompleteFor(type: ReplCommandParameterDescriptor<*>): List<ReplExecutor.AutoComplete> {
        return handler(type)
    }
}
