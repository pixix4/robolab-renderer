package de.robolab.client.repl

import de.robolab.client.repl.base.IReplCommandParameter
import de.robolab.client.repl.base.IReplCommandParameterTypeDescriptor
import de.robolab.client.repl.base.escapeIfNecessary
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

data class IntParameter(
    val value: Int,
) : IReplCommandParameter {

    override val typeDescriptor: IReplCommandParameterTypeDescriptor<*> = Companion

    override fun toToken(): String = value.toString()

    companion object : IReplCommandParameterTypeDescriptor<IntParameter> {
        override val klazz: KClass<IntParameter> = IntParameter::class
        override val name: String = "Int"
        override val description = "A integer value"
        override val pattern = "<digits>"
        override val example = listOf<String>()
        override val regex: Regex = """\d+""".toRegex()

        override fun fromToken(token: String): IntParameter? {
            return IntParameter(token.toIntOrNull() ?: return null)
        }
    }
}

data class DoubleParameter(
    val value: Double,
) : IReplCommandParameter {

    override val typeDescriptor: IReplCommandParameterTypeDescriptor<*> = Companion

    override fun toToken(): String = value.toString()

    companion object : IReplCommandParameterTypeDescriptor<DoubleParameter> {
        override val klazz: KClass<DoubleParameter> = DoubleParameter::class
        override val name: String = "Double"
        override val description = "A double value"
        override val pattern = "<digits.digits>"
        override val example = listOf<String>()
        override val regex: Regex = """\d+(\.\d+)?""".toRegex()

        override fun fromToken(token: String): DoubleParameter? {
            return DoubleParameter(token.toDoubleOrNull() ?: return null)
        }
    }
}

data class StringParameter(
    val value: String,
) : IReplCommandParameter {

    override val typeDescriptor: IReplCommandParameterTypeDescriptor<*> = Companion

    override fun toToken(): String = value.escapeIfNecessary()

    companion object : IReplCommandParameterTypeDescriptor<StringParameter> {
        override val klazz: KClass<StringParameter> = StringParameter::class
        override val name: String = "String"
        override val description = "A string value"
        override val pattern = "<characters>"
        override val example = listOf<String>()
        override val regex: Regex = """.*""".toRegex()

        override fun fromToken(token: String): StringParameter? {
            return StringParameter(token)
        }
    }
}
