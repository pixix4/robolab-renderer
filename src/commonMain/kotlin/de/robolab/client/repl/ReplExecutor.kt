package de.robolab.client.repl

import de.robolab.client.app.controller.ui.indexOfOrNull
import de.robolab.client.repl.base.*
import de.robolab.client.repl.commands.RootCommand

object ReplExecutor {

    data class AutoComplete(
        val name: String,
        val suffix: String,
        val description: String,
    ) {
        constructor(suffix: String, description: String) : this(suffix, suffix, description)
    }

    data class Hint(
        val highlight: List<HintHighlight>,
        val suffix: String,
        val suffixHighlight: List<HintHighlight>,
        val input: String = "",
    )

    data class HintHighlight(
        val range: IntRange,
        val color: ReplColor,
    )

    data class Token(
        val token: String,
        val value: String,
        val range: IntRange,
        val index: Int,
    ) {

        fun removeEscape(open: Boolean = false): Token {
            val v = if (value.startsWith('"') && value.endsWith('"')) {
                value.removeSurrounding("\"")
            } else if (value.startsWith('\'') && value.endsWith('\'')) {
                value.removeSurrounding("'")
            } else {
                if (open) {
                    if (value.startsWith('"')) {
                        value.removePrefix("\"")
                    } else if (value.startsWith('\'')) {
                        value.removePrefix("'")
                    } else {
                        value
                    }
                } else {
                    value
                }
            }

            return copy(value = v)
        }

        companion object {
            operator fun invoke(token: String, range: IntRange, index: Int, open: Boolean = false): Token {
                return Token(token, token, range, index).removeEscape(open)
            }
        }
    }

    data class AutoCompleteJob(
        val parameter: ReplCommandParameterDescriptor<*>,
    )

    private var activeAutoComplete: AutoCompleteJob? = null

    suspend fun execute(input: String, output: IReplOutput) {
        try {
            execute(bindTokenList(tokenize(input), output))
        } catch (e: Exception) {
            output.writeln("Command could not be parsed: ${e::class.simpleName ?: "Exception"}: ${e.message}",
                ReplColor.RED)
        }
    }

    suspend fun autoComplete(input: String): List<AutoComplete> {
        return try {
            autoComplete(bindTokenList(tokenize(input)))
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun hint(input: String): Hint {
        return try {
            hint(bindTokenList(tokenize(input))).copy(input = input)
        } catch (e: Exception) {
            Hint(emptyList(), "", emptyList(), input)
        }
    }

    sealed class TokenBinding(
        val token: Token,
        val color: ReplColor? = null,
    ) {

        class Leaf(
            token: Token,
            val command: ReplBindableLeafCommand<*>,
            val binding: Any,
            val commandBinding: CommandBinding<*>,
        ) : TokenBinding(token, ReplColor.CYAN) {

            suspend fun execute(context: IReplExecutionContext) {
                commandBinding.execute(context)
            }

            override fun toString(): String {
                return "Leaf(${command.name})"
            }
        }

        class Node(
            token: Token,
            val command: ReplBindableNodeCommand<*>,
            val binding: Any,
            val commandBinding: CommandBinding<*>,
        ) : TokenBinding(token, ReplColor.BLUE) {

            override fun toString(): String {
                return "Node(${command.name})"
            }
        }

        class Management(
            token: Token,
            val command: ReplManagementCommand,
        ) : TokenBinding(token, ReplColor.YELLOW) {

            override fun toString(): String {
                return "Management(${command.name})"
            }
        }

        class Parameter(
            token: Token,
            val parameter: ReplCommandParameterDescriptor<*>,
            val match: MatchResult,
        ) : TokenBinding(token, ReplColor.GREEN) {

            override fun toString(): String {
                return "Parameter(${parameter.name})"
            }
        }

        class Open(
            token: Token,
        ) : TokenBinding(token) {

            override fun toString(): String {
                return "Open"
            }
        }

        class Error(
            token: Token,
        ) : TokenBinding(token, ReplColor.RED) {

            override fun toString(): String {
                return "Error"
            }
        }
    }

    class ExecutionContext(
        val output: IReplOutput,
        val tokenCommandList: List<TokenBinding>,
    ) : IReplExecutionContext, IReplOutput by output {

        val containsErrors = tokenCommandList.any { it is TokenBinding.Error }

        val commandChain: List<CommandBinding<*>>
            get() = tokenCommandList.mapNotNull {
                when (it) {
                    is TokenBinding.Leaf -> it.commandBinding
                    is TokenBinding.Node -> it.commandBinding
                    else -> null
                }
            }

        fun getHighlighting(): List<HintHighlight> {
            return tokenCommandList.mapNotNull {
                HintHighlight(it.token.range, it.color ?: return@mapNotNull null)
            }.filterNot {
                it.range.isEmpty()
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : IReplCommandParameter> getGeneralParameter(parameter: ReplCommandParameterDescriptor<T>): List<T> {
            return tokenCommandList.mapNotNull {
                if (it is TokenBinding.Parameter) {
                    if (it.parameter == parameter) {
                        parameter.type.fromToken(it.token.value, it.match)
                    } else null
                } else null
            }
        }

        suspend fun execute() {
            val management = tokenCommandList.filterIsInstance<TokenBinding.Management>().firstOrNull()
            if (management != null) {
                return management.command.execute(commandChain, this)
            }

            val leaf = tokenCommandList.filterIsInstance<TokenBinding.Leaf>().firstOrNull()
            leaf?.execute(this)
        }
    }

    private suspend fun execute(context: ExecutionContext) {
        if (context.containsErrors) {
            throw IllegalStateException("The given input is not a valid command!")
        }

        try {
            context.execute()
        } catch (e: Exception) {
            context.writeln("Command failed with ${e::class.simpleName ?: "Exception"}: ${e.message}", ReplColor.RED)
        }
    }

    fun cancelAutoComplete() {
        activeAutoComplete = null
    }

    private suspend fun autoComplete(context: ExecutionContext): List<AutoComplete> {
        cancelAutoComplete()

        val lastNode = context.tokenCommandList.filterIsInstance<TokenBinding.Node>().last()
        val lastLeaf = context.tokenCommandList.filterIsInstance<TokenBinding.Leaf>().lastOrNull()
        val lastBinding = context.tokenCommandList.last()
        val isManagementCommand = context.tokenCommandList.getOrNull(1) is TokenBinding.Management

        if (lastLeaf != null) {
            val parameterToken = context.tokenCommandList.dropWhile { it != lastLeaf }.drop(1)
            val parameterList = lastLeaf.command.parameters

            val index = parameterToken.indexOfOrNull(lastBinding) ?: 0
            val value = parameterToken.getOrNull(index)?.token?.value ?: ""
            val parameter = parameterList.getOrNull(index) ?: parameterList.lastOrNull()
                ?.takeIf { it is ReplCommandVarargParameterDescriptor }

            if (parameter != null) {
                activeAutoComplete = AutoCompleteJob(parameter)
                for (c in context.commandChain.reversed()) {
                    val auto = c.requestAutoCompleteFor(parameter, value)

                    if (auto != null) {
                        activeAutoComplete = null
                        return auto
                    }
                }
                activeAutoComplete = null
            }
            return emptyList()
        }

        val diff = if (isManagementCommand) 3 else 2
        if (lastBinding.token.index - lastNode.token.index >= diff) {
            return emptyList()
        }

        val value = lastBinding.token.value

        val next = lastNode.command.children.map { command ->
            val names = command.getAvailableNames().filter {
                it.startsWith(value)
            }

            names.map { it to command.description }
        }.flatten()

        return next.map {
            AutoComplete(
                it.first,
                it.first.removePrefix(value),
                it.second
            )
        }
    }

    private fun hint(context: ExecutionContext): Hint {
        val lastNode = context.tokenCommandList.filterIsInstance<TokenBinding.Node>().last()
        val lastLeaf = context.tokenCommandList.filterIsInstance<TokenBinding.Leaf>().lastOrNull()
        val lastBinding = context.tokenCommandList.last()
        val isManagementCommand = context.tokenCommandList.getOrNull(1) is TokenBinding.Management

        if (lastLeaf != null) {
            val parameterToken = context.tokenCommandList.dropWhile { it != lastLeaf }.drop(1)
            val parameterList = lastLeaf.command.parameters

            var index = parameterToken.indexOfOrNull(lastBinding) ?: 0
            if (lastBinding.token.value != "") {
                index += 1
            }
            val remainingParameterList = parameterList.drop(index)

            val paramsList = remainingParameterList.map {
                val s = it.name
                (if (it is ReplCommandOptionalParameterDescriptor<*>) "[$s]" else "<$s>") to (it == activeAutoComplete?.parameter)
            }

            val suffixBuilder = StringBuilder()
            val suffixHighlight = mutableListOf<HintHighlight>()

            if (lastBinding.token.value != "") {
                suffixBuilder.append(" ")
            }
            for ((value, active) in paramsList) {
                val start = suffixBuilder.length
                suffixBuilder.append(value)
                suffixBuilder.append(" ")
                if (active) {
                    suffixHighlight += HintHighlight(start until (start + value.length), ReplColor.MAGENTA)
                }
            }

            return Hint(
                context.getHighlighting(),
                suffixBuilder.toString().trimEnd(),
                suffixHighlight
            )
        }

        val diff = if (isManagementCommand) 3 else 2
        if (lastBinding.token.index - lastNode.token.index >= diff) {
            return Hint(context.getHighlighting(), "", emptyList())
        }

        val value = lastBinding.token.value

        val next = lastNode.command.children.map { command ->
            command.getAvailableNames().filter {
                it.startsWith(value)
            }
        }.flatten()

        return Hint(context.getHighlighting(), (next.firstOrNull() ?: "").removePrefix(value), emptyList())
    }

    private fun tokenize(input: String): List<Token> {
        val trimmedInput = input.trimStart()
        val rangeOffset = input.length - trimmedInput.length

        val regex = """'(?:[^'\\]|\\.)*'|"(?:[^"\\]|\\.)*"|([^ '"\\])+""".toRegex()

        // parse string to token list
        var tokenList = regex.findAll(trimmedInput).mapIndexed { index, matchResult ->
            Token(matchResult.value, matchResult.value, matchResult.range.offset(rangeOffset), index)
        }.toList()

        // Append trailing token
        if (input.endsWith(" ") || input.isEmpty()) {
            tokenList = tokenList + Token("",
                (trimmedInput.length..trimmedInput.length).offset(rangeOffset),
                tokenList.size,
                true)
        }

        // Remove escape characters
        tokenList = tokenList.mapIndexed { index, token ->
            token.removeEscape(index == tokenList.lastIndex)
        }

        return tokenList
    }

    private fun bindTokenList(input: List<Token>, output: IReplOutput = DummyReplOutput): ExecutionContext {
        if (input.isEmpty()) return ExecutionContext(output, emptyList())

        var currentCommandBinding: CommandBinding<*> = ROOT_BINDING
        var parameterIndex = 0
        val tokenCommandList = mutableListOf<TokenBinding>(
            TokenBinding.Node(Token("", 0 until 0, -1), RootCommand, Unit, ROOT_BINDING)
        )

        for ((index, token) in input.withIndex()) {
            val istLastToken = index == input.lastIndex
            val current = currentCommandBinding

            tokenCommandList += when (current.command) {
                is ReplBindableLeafCommand -> {
                    val param =
                        current.command.parameters.getOrNull(parameterIndex) ?: current.command.parameters.lastOrNull()
                            ?.takeIf { it is ReplCommandVarargParameterDescriptor }
                    parameterIndex += 1

                    if (param != null) {
                        val matchResult = param.type.regex.matchEntire(token.value)

                        if (matchResult != null) {
                            TokenBinding.Parameter(token, param, matchResult)
                        } else if (istLastToken) {
                            TokenBinding.Open(token)
                        } else {
                            TokenBinding.Error(token)
                        }
                    } else if (istLastToken && token.value.isEmpty()) {
                        TokenBinding.Open(token)
                    } else {
                        TokenBinding.Error(token)
                    }
                }
                is ReplBindableNodeCommand -> {
                    var next: TokenBinding? = null

                    for (child in current.command.children) {
                        val binding = child.getBindingByName(token.value, current.binding)
                        val bindingCommand = binding?.command

                        next = when (bindingCommand) {
                            is ReplBindableLeafCommand -> {
                                currentCommandBinding = binding
                                TokenBinding.Leaf(token, bindingCommand, binding.binding, binding)
                            }
                            is ReplBindableNodeCommand -> {
                                currentCommandBinding = binding
                                TokenBinding.Node(token, bindingCommand, binding.binding, binding)
                            }
                            is ReplManagementCommand -> TokenBinding.Management(token, bindingCommand)
                            null -> null
                        }

                        if (next != null) break
                    }

                    next ?: if (istLastToken) {
                        TokenBinding.Open(token)
                    } else {
                        TokenBinding.Error(token)
                    }
                }
                is ReplManagementCommand -> {
                    throw IllegalStateException("ReplManagementCommand cannot be bound!")
                }
            }
        }

        return ExecutionContext(output, tokenCommandList)
    }

    private val ROOT_BINDING: CommandBinding<*> = CommandBinding(RootCommand, Unit)
}
