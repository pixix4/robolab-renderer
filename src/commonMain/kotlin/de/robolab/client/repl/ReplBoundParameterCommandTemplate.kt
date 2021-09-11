package de.robolab.client.repl

import de.robolab.client.repl.base.IReplCommandLeaf
import de.robolab.client.repl.base.IReplCommandParameter
import de.robolab.client.repl.base.IReplOutput
import de.robolab.client.repl.base.ReplCommandParameterDescriptor
import io.ktor.utils.io.core.*

open class ReplBoundParameterCommandTemplate<T>(
    val name: String,
    val description: String,
    vararg val parameters: ReplCommandParameterDescriptor<*>,
    private val executeHandler: suspend T.(out: IReplOutput, params: List<IReplCommandParameter>) -> Unit,
) {

    fun bind(ref: T): IReplCommandLeaf = ReplParameterCommand(
        name,
        description,
        *parameters
    ) { out, params ->
        executeHandler(ref, out, params)
    }
}
