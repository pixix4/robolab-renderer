package de.robolab.client.repl

import de.robolab.client.repl.base.ReplCommandParameterDescriptor
import de.robolab.client.repl.base.IReplCommandLeaf
import de.robolab.client.repl.base.IReplCommandParameter
import de.robolab.client.repl.base.IReplOutput

open class ReplParameterCommand(
    override val name: String,
    override val description: String,
    vararg parameters: ReplCommandParameterDescriptor<*>,
    private val executeHandler: suspend (output: IReplOutput, params: List<IReplCommandParameter>) -> Unit,
): IReplCommandLeaf {
    override val parameters: List<ReplCommandParameterDescriptor<*>> = parameters.toList()

    override suspend fun execute(output: IReplOutput, parameters: List<String>) {
        val p = this.parameters.mapIndexed { i, p ->
            val nextString = parameters.getOrNull(i)
            if (nextString == null) {
                if (p.optional) {
                    p to null
                } else {
                    throw IllegalArgumentException("Required parameter '${p.name}' is missing!")
                }
            } else {
                if (p.type.regex.matches(nextString)) {
                    p to nextString
                } else {
                    throw IllegalArgumentException("Required parameter '${p.name}' does not match the given token '$nextString'!")
                }
            }
        }.mapNotNull { (type, param) ->
            if (param == null) null else {
                type.type.fromToken(param)
            }
        }

        return executeHandler(output, p)
    }
}
