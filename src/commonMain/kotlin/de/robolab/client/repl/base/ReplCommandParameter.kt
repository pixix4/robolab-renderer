package de.robolab.client.repl.base

import de.robolab.client.repl.escapeIfNecessary
import kotlin.reflect.KClass

interface IReplCommandParameter {
    fun toToken(): String
}

interface IReplCommandParameterTypeDescriptor<T> where T : IReplCommandParameter {

    val klazz: KClass<T>

    val name: String
    val description: String
    val example: List<String>
    val pattern: String
    val regex: Regex

    fun fromToken(token: String, match: MatchResult): T?
}

sealed interface ReplCommandParameterDescriptor<T : IReplCommandParameter> {
    val type: IReplCommandParameterTypeDescriptor<T>
    val name: String
}

data class ReplCommandDefaultParameterDescriptor<T : IReplCommandParameter>(
    override val type: IReplCommandParameterTypeDescriptor<T>,
    override val name: String,
) : ReplCommandParameterDescriptor<T>

data class ReplCommandOptionalParameterDescriptor<T : IReplCommandParameter>(
    override val type: IReplCommandParameterTypeDescriptor<T>,
    override val name: String,
) : ReplCommandParameterDescriptor<T>

data class ReplCommandVarargParameterDescriptor<T : IReplCommandParameter>(
    override val type: IReplCommandParameterTypeDescriptor<T>,
    override val name: String,
) : ReplCommandParameterDescriptor<T>

data class BooleanParameter(
    val value: Boolean,
) : IReplCommandParameter {

    override fun toToken(): String = value.toString()

    companion object : IReplCommandParameterTypeDescriptor<BooleanParameter> {
        override val klazz: KClass<BooleanParameter> = BooleanParameter::class
        override val name: String = "Boolean"
        override val description = "A boolean value"
        override val pattern = "<true|false>"
        override val example = listOf<String>()
        override val regex: Regex = """(true|false)""".toRegex()

        override fun fromToken(token: String, match: MatchResult): BooleanParameter? {
            return BooleanParameter(token.toBooleanStrictOrNull() ?: return null)
        }
    }
}

data class IntParameter(
    val value: Int,
) : IReplCommandParameter {

    override fun toToken(): String = value.toString()

    companion object : IReplCommandParameterTypeDescriptor<IntParameter> {
        override val klazz: KClass<IntParameter> = IntParameter::class
        override val name: String = "Int"
        override val description = "A integer value"
        override val pattern = "<digits>"
        override val example = listOf<String>()
        override val regex: Regex = """-?\d+""".toRegex()

        override fun fromToken(token: String, match: MatchResult): IntParameter? {
            return IntParameter(token.toIntOrNull() ?: return null)
        }
    }
}

data class DoubleParameter(
    val value: Double,
) : IReplCommandParameter {

    override fun toToken(): String = value.toString()

    companion object : IReplCommandParameterTypeDescriptor<DoubleParameter> {
        override val klazz: KClass<DoubleParameter> = DoubleParameter::class
        override val name: String = "Double"
        override val description = "A double value"
        override val pattern = "<digits.digits>"
        override val example = listOf<String>()
        override val regex: Regex = """-?\d+(\.\d+)?""".toRegex()

        override fun fromToken(token: String, match: MatchResult): DoubleParameter? {
            return DoubleParameter(token.toDoubleOrNull() ?: return null)
        }
    }
}

data class StringParameter(
    val value: String,
) : IReplCommandParameter {

    override fun toToken(): String = value.escapeIfNecessary()

    companion object : IReplCommandParameterTypeDescriptor<StringParameter> {
        override val klazz: KClass<StringParameter> = StringParameter::class
        override val name: String = "String"
        override val description = "A string value"
        override val pattern = "<characters>"
        override val example = listOf<String>()
        override val regex: Regex = """.*""".toRegex()

        override fun fromToken(token: String, match: MatchResult): StringParameter? {
            return StringParameter(token)
        }
    }
}

data class EnumParameter<T : Enum<T>>(
    val value: T,
) : IReplCommandParameter {

    override fun toToken(): String = value.toString().lowercase()

    class Descriptor<T : Enum<T>>(
        override val name: String,
        override val description: String,
        val valueList: List<T>,
    ) : IReplCommandParameterTypeDescriptor<EnumParameter<T>> {
        private val valueStringList = valueList.map { it.name.lowercase() }

        override val klazz: KClass<EnumParameter<T>> = EnumParameter::class as KClass<EnumParameter<T>>
        override val pattern = valueStringList
            .joinToString("|", "<", ">")
        override val example = listOf<String>()
        override val regex: Regex = valueStringList
            .joinToString("|", "(", ")").toRegex()

        override fun fromToken(token: String, match: MatchResult): EnumParameter<T>? {
            val value = valueList
                .find { it.name.equals(token, true) } ?: return null
            return EnumParameter(value)
        }
    }

    companion object {
        inline fun <reified T : Enum<T>> create(
            description: String,
            name: String = T::class.simpleName ?: "",
        ): Descriptor<T> {
            return Descriptor(name, description, enumValues<T>().toList())
        }

        inline fun <reified T : Enum<T>> isDescriptor(descriptor: IReplCommandParameterTypeDescriptor<*>): Boolean {
            if (descriptor !is Descriptor<*>) return false
            val list = enumValues<T>().toList()
            return descriptor.valueList.containsAll(list) && list.containsAll(descriptor.valueList)
        }
    }
}
