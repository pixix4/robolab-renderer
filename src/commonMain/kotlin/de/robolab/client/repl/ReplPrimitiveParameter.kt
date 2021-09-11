package de.robolab.client.repl

import de.robolab.client.repl.base.IReplCommandParameter
import de.robolab.client.repl.base.IReplCommandParameterTypeDescriptor
import kotlin.reflect.KClass

data class BooleanParameter(
    val value: Boolean,
) : IReplCommandParameter {

    override val typeDescriptor: IReplCommandParameterTypeDescriptor<*> = Companion

    override fun toToken(): String = value.toString()

    companion object : IReplCommandParameterTypeDescriptor<BooleanParameter> {
        override val klazz: KClass<BooleanParameter> = BooleanParameter::class
        override val name: String = "Boolean"
        override val description = "A boolean value"
        override val pattern = "<true|false>"
        override val example = listOf<String>()
        override val regex: Regex = """(true|false)""".toRegex()

        override fun fromToken(token: String): BooleanParameter? {
            return BooleanParameter(token.toBooleanStrictOrNull() ?: return null)
        }
    }
}
