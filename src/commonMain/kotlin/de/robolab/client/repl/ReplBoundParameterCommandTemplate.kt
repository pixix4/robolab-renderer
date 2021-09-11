package de.robolab.client.repl

import de.robolab.client.repl.base.*
import io.ktor.utils.io.core.*

abstract class ReplBoundParameterCommandTemplate<T>(
    val name: String,
    val description: String,
    vararg val parameters: ReplCommandParameterDescriptor<*>,
) : IReplBoundCommandTemplate<T> {

    override fun bind(ref: T): IReplCommandLeaf = ReplParameterCommand(
        name,
        description,
        *parameters
    ) { out, params ->
        ref.execute(out, params)
    }

    abstract suspend fun T.execute(out: IReplOutput, params: List<IReplCommandParameter>)

    companion object {
        operator fun <T> invoke(
            name: String,
            description: String,
            vararg parameters: ReplCommandParameterDescriptor<*>,
            executeHandler: suspend T.(out: IReplOutput, params: List<IReplCommandParameter>) -> Unit
        ) = object : ReplBoundParameterCommandTemplate<T>(name, description, *parameters) {
            override suspend fun T.execute(out: IReplOutput, params: List<IReplCommandParameter>) =
                executeHandler(this, out, params)
        }
    }
}
