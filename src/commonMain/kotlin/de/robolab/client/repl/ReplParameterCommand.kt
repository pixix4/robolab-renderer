package de.robolab.client.repl

import de.robolab.client.repl.base.ReplCommandParameterDescriptor
import de.robolab.client.repl.base.IReplCommandLeaf
import de.robolab.client.repl.base.IReplCommandParameter

open class ReplParameterCommand(
    override val name: String,
    override val description: String,
    vararg parameters: ReplCommandParameterDescriptor<*>,
    private val executeHandler: suspend (params: List<IReplCommandParameter>) -> List<String>,
): IReplCommandLeaf {
    override val parameters: List<ReplCommandParameterDescriptor<*>> = parameters.toList()

    override suspend fun execute(parameters: List<String>): List<String> {
        val stringParameters = parameters.toMutableList()

        val p = this.parameters.map { p ->
            val nextString = stringParameters.firstOrNull()
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
                    throw IllegalArgumentException("Required parameter '${p.name}' does not match given '$nextString'!")
                }
            }
        }.mapNotNull { (type, param) ->
            if (param == null) null else {
                type.type.fromToken(param)
            }
        }

        return executeHandler(p)
    }
}
