package de.robolab.client.repl

import de.robolab.client.repl.base.*

object ReplExecutor {

    data class AutoComplete(
        val name: String,
        val suffix: String,
        val description: String,
    )

    data class Hint(
        val suffix: String,
        val highlight: List<HintHighlight>,
        val input: String = "",
    )

    data class HintHighlight(
        val range: IntRange,
        val color: HintColor,
    )

    enum class HintColor {
        NODE, LEAF, PARAMETER, ERROR
    }

    data class Token(
        val token: String,
        val value: String,
        val range: IntRange,
    ) {

        fun highlight(color: HintColor): HintHighlight {
            return HintHighlight(
                range,
                color
            )
        }

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
            operator fun invoke(token: String, range: IntRange, open: Boolean = false): Token {
                return Token(token, token, range).removeEscape(open)
            }
        }
    }

    suspend fun execute(input: String, output: IReplOutput) {
        execute(
            output,
            ReplRootCommand,
            tokenize(input),
            emptyList()
        )
    }

    fun autoComplete(input: String): List<AutoComplete> {
        return autoComplete(
            ReplRootCommand,
            tokenize(input, true)
        )
    }

    fun hint(input: String): Hint {
        return hint(
            ReplRootCommand,
            tokenize(input, true),
            emptyList()
        ).copy(input = input)
    }

    private suspend fun execute(
        output: IReplOutput,
        command: IReplCommand,
        input: List<Token>,
        parentNames: List<String>,
    ) {
        val nextInput = input.firstOrNull()

        if (nextInput != null) {
            if (nextInput.value == "help") {
                return command.printHelp(output, parentNames)
            }

            if (command is IReplCommandNode) {
                val subCommand = command.commands.filter {
                    it.name == nextInput.value
                }.merge()

                if (subCommand != null) {
                    val nextParentNames = if (command is ReplRootCommand) {
                        parentNames
                    } else {
                        parentNames + command.name
                    }

                    return execute(
                        output,
                        subCommand,
                        input.drop(1),
                        nextParentNames
                    )
                }
            }
        }

        if (command is IReplCommandLeaf) {
            try {
                command.execute(output, input.map { it.value })
            } catch (e: Exception) {
                listOf(
                    "Command failed with ${e::class.simpleName ?: "Exception"}: ${e.message}"
                )
            }
        } else {
            if (nextInput != null) {
                if (command is ReplRootCommand) {
                    listOf("Unknown command '${(parentNames + nextInput.value).joinToString(" ")}'!") + command.printHelp(
                        output, parentNames)
                } else {
                    listOf("Unknown command '${(parentNames + command.name + nextInput.value).joinToString(" ")}'!") + command.printHelp(
                        output, parentNames)
                }
            } else {
                command.printHelp(output, parentNames)
            }
        }
    }


    private fun autoComplete(command: IReplCommand, input: List<Token>): List<AutoComplete> {
        val nextInput = input.firstOrNull()

        if (nextInput != null) {
            if (nextInput.value == "help") {
                return emptyList()
            }

            if (command is IReplCommandNode) {
                val subCommand = command.commands.filter {
                    it.name == nextInput.value
                }.merge()

                if (subCommand != null) {
                    return autoComplete(
                        subCommand,
                        input.drop(1),
                    )
                }

                val subCommands = command.helpCommandDescriptions.filter {
                    it.first.startsWith(nextInput.value)
                }

                if (subCommands.isNotEmpty()) {
                    return subCommands.map {
                        AutoComplete(
                            it.first,
                            it.first.removePrefix(nextInput.value),
                            it.second
                        )
                    }
                }
            }
        }

        return emptyList()
    }

    private fun hint(command: IReplCommand, input: List<Token>, highlight: List<HintHighlight>): Hint {
        val nextToken = input.firstOrNull()
        val nextNextToken = input.getOrNull(1)

        if (nextToken != null) {
            if (nextToken.value == "help") {
                return Hint("", highlight + nextToken.highlight(HintColor.LEAF))
            }

            if (command is IReplCommandNode) {
                val subCommand = command.commands.filter {
                    it.name == nextToken.value
                }.merge()

                if (subCommand != null) {
                    val h = when (subCommand) {
                        is IReplCommandLeaf -> highlight + nextToken.highlight(HintColor.LEAF)
                        is IReplCommandNode -> highlight + nextToken.highlight(HintColor.NODE)
                        else -> highlight
                    }
                    return hint(
                        subCommand,
                        input.drop(1),
                        h
                    )
                }

                val subCommands = command.helpCommandDescriptions.filter {
                    it.first.startsWith(nextToken.value)
                }

                if (subCommands.isNotEmpty() && nextNextToken == null) {
                    if (command is ReplRootCommand && nextToken.value == "") {
                        return Hint(
                            "help",
                            highlight
                        )
                    }
                    return Hint(
                        subCommands.first().first.removePrefix(nextToken.value),
                        highlight
                    )
                } else if (nextToken.value.isNotEmpty()) {
                    return Hint(
                        "",
                        highlight + nextToken.highlight(HintColor.ERROR)
                    )
                }
            }
        }

        if (command is IReplCommandLeaf) {
            val paramHighlight = input.mapIndexed { i, token ->
                val type = command.parameters.getOrNull(i)
                if (type?.type?.regex?.matches(token.value) == true) {
                    token.highlight(HintColor.PARAMETER)
                } else {
                    token.highlight(HintColor.ERROR)
                }
            }

            val params = command.parameters.map {
                val s = it.name
                if (it.optional) "[$s]" else "<$s>"
            }.drop(input.count { it.value.isNotEmpty() }).joinToString(" ")

            return Hint(
                if (input.lastOrNull()?.value == "") params else " $params",
                highlight + paramHighlight
            )
        }

        return Hint("", highlight)
    }

    private fun tokenize(input: String, open: Boolean = false): List<Token> {
        var trimmedInput = input.trimStart()
        val rangeOffset = input.length - trimmedInput.length

        if (open && trimmedInput.isEmpty()) {
            return listOf(Token("", (0..0).offset(rangeOffset), true))
        }

        if (!open) {
            trimmedInput = trimmedInput.trimEnd()
        }

        val regex = if (open) {
            """'(?:[^'\\]|\\.)*('|$)|"(?:[^"\\]|\\.)*("|$)|([^ '"\\])+""".toRegex()
        } else {
            """'(?:[^'\\]|\\.)*'|"(?:[^"\\]|\\.)*"|([^ '"\\])+""".toRegex()
        }
        var tokenList = regex.findAll(trimmedInput).map {
            Token(it.value, it.range.offset(rangeOffset), true)
        }.toList()
        if (open && trimmedInput.endsWith(" ")) {
            tokenList = tokenList + Token("", (trimmedInput.length..trimmedInput.length).offset(rangeOffset), true)
        }

        return tokenList
    }
}

fun IntRange.offset(offset: Int) = IntRange(start + offset, endInclusive + offset)

fun ReplExecutor.HintColor.toColor() = when (this) {
    ReplExecutor.HintColor.NODE -> ReplColor.BLUE
    ReplExecutor.HintColor.LEAF -> ReplColor.CYAN
    ReplExecutor.HintColor.PARAMETER -> ReplColor.GREEN
    ReplExecutor.HintColor.ERROR -> ReplColor.RED
}
