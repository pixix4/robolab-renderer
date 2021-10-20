package de.robolab.client.repl.base

import de.robolab.client.repl.ReplExecutor
import kotlin.reflect.KClass

sealed class ReplBindableCommand<T : Any>(
    val name: String,
    val description: String,
    private val bindingType: KClass<T>,
) {

    protected fun acceptBinding(binding: Any): Boolean {
        return bindingType.isInstance(binding)
    }

    abstract fun getBindingByName(name: String, parentBinding: Any): CommandBinding<T>?

    abstract fun getAvailableNames(): List<String>

    open suspend fun requestAutoCompleteFor(
        binding: T,
        descriptor: ReplCommandParameterDescriptor<*>,
        token: String,
    ): List<ReplExecutor.AutoComplete>? {
        return null
    }
}

abstract class ReplBindableLeafCommand<T : Any>(
    name: String,
    description: String,
    bindingType: KClass<T>,
) : ReplBindableCommand<T>(name, description, bindingType) {

    override fun getBindingByName(name: String, parentBinding: Any): CommandBinding<T>? {
        if (this.name == name) {
            if (acceptBinding(parentBinding)) {
                @Suppress("UNCHECKED_CAST")
                return CommandBinding(this, parentBinding as T)
            }
        }

        return null
    }

    override fun getAvailableNames(): List<String> {
        return listOf(name)
    }

    private val parameterList = mutableListOf<ReplCommandParameterDescriptor<*>>()

    val parameters: List<ReplCommandParameterDescriptor<*>>
        get() = parameterList

    abstract suspend fun execute(binding: T, context: IReplExecutionContext)

    fun <T : IReplCommandParameter> IReplCommandParameterTypeDescriptor<T>.param(
        name: String,
    ): ReplCommandDefaultParameterDescriptor<T> {
        val param = ReplCommandDefaultParameterDescriptor(this, name)
        parameterList += param
        return param
    }

    fun <T : IReplCommandParameter> IReplCommandParameterTypeDescriptor<T>.optional(
        name: String,
    ): ReplCommandOptionalParameterDescriptor<T> {
        val param = ReplCommandOptionalParameterDescriptor(this, name)
        parameterList += param
        return param
    }

    fun <T : IReplCommandParameter> IReplCommandParameterTypeDescriptor<T>.vararg(
        name: String,
    ): ReplCommandVarargParameterDescriptor<T> {
        val param = ReplCommandVarargParameterDescriptor(this, name)
        parameterList += param
        return param
    }
}


sealed class ReplBindableNodeCommand<T : Any>(
    name: String,
    description: String,
    bindingType: KClass<T>,
) : ReplBindableCommand<T>(name, description, bindingType) {

    private val childrenList = mutableListOf<ReplBindableCommand<*>>()
    val children: List<ReplBindableCommand<*>>
        get() = childrenList

    fun addCommand(command: ReplBindableCommand<*>) {
        childrenList += command
    }
}

abstract class ReplSingleBindableNodeCommand<T : Any>(
    name: String,
    description: String,
    bindingType: KClass<T>,
) : ReplBindableNodeCommand<T>(name, description, bindingType) {

    override fun getBindingByName(name: String, parentBinding: Any): CommandBinding<T>? {
        if (this.name == name) {
            val binding = binding

            if (binding != null) {
                return CommandBinding(this, binding)
            } else if (acceptBinding(parentBinding)) {
                @Suppress("UNCHECKED_CAST")
                return CommandBinding(this, parentBinding as T)
            }
        }

        return null
    }

    override fun getAvailableNames(): List<String> {
        if (binding == null) return emptyList()
        return listOf(name)
    }

    private var binding: T? = null

    fun bind(binding: T?) {
        this.binding = binding
    }

    fun unbind() = bind(null)
}

abstract class ReplMultiBindableNodeCommand<T : Any>(
    name: String,
    description: String,
    bindingType: KClass<T>,
) : ReplBindableNodeCommand<T>(name, description, bindingType) {

    override fun getBindingByName(name: String, parentBinding: Any): CommandBinding<T>? {
        val nameList = bindingList.map { it to this.name + getBindingName(it) }
        val (binding, _) = nameList.find { it.second == name } ?: return null

        return CommandBinding(this, binding)
    }

    override fun getAvailableNames(): List<String> {
        return bindingList.map {
            "$name:${getBindingName(it)}"
        }
    }

    private val bindingList = mutableListOf<T>()

    abstract fun getBindingName(binding: T): String

    fun bind(binding: T) {
        if (binding in bindingList) {
            return
        }

        bindingList += binding
    }

    fun unbind(binding: T) {
        bindingList -= binding
    }

    fun unbindAll() {
        bindingList.clear()
    }
}

data class CommandBinding<T : Any>(
    val command: ReplBindableCommand<T>,
    val binding: T,
) {

    val name: String
        get() = when (command) {
            is ReplMultiBindableNodeCommand -> command.name + command.getBindingName(binding)
            else -> command.name
        }


    suspend fun requestAutoCompleteFor(
        descriptor: ReplCommandParameterDescriptor<*>,
        token: String,
    ): List<ReplExecutor.AutoComplete>? {
        return command.requestAutoCompleteFor(binding, descriptor, token)
    }


    suspend fun execute(context: IReplExecutionContext) {
        (command as? ReplBindableLeafCommand<T>)?.execute(binding, context)
    }
}

abstract class ReplManagementCommand(
    name: String,
    description: String,
) : ReplBindableCommand<Unit>(name, description, Unit::class) {

    abstract fun execute(commandChain: List<CommandBinding<*>>, context: IReplExecutionContext)

    override fun getBindingByName(name: String, parentBinding: Any): CommandBinding<Unit>? {
        if (name == this.name) {
            return CommandBinding(this, Unit)
        }

        return null
    }

    override fun getAvailableNames(): List<String> {
        return listOf(name)
    }
}
