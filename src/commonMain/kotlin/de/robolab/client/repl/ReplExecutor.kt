package de.robolab.client.repl

import de.robolab.client.repl.base.IReplCommand
import de.robolab.client.repl.base.IReplCommandLeaf
import de.robolab.client.repl.base.IReplCommandNode

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

            return Token(v, range)
        }
    }

    suspend fun execute(input: String): List<String> {
        return execute(
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

    private suspend fun execute(command: IReplCommand, input: List<Token>, parentNames: List<String>): List<String> {
        val nextInput = input.firstOrNull()

        if (nextInput != null) {
            if (nextInput.value == "help") {
                return command.printHelp(parentNames)
            }

            if (command is IReplCommandNode) {
                val subCommand = command.commands.find {
                    it.name == nextInput.value
                }

                if (subCommand != null) {
                    val nextParentNames = if (command is ReplRootCommand) {
                        parentNames
                    } else {
                        parentNames + command.name
                    }

                    return execute(
                        subCommand,
                        input.drop(1),
                        nextParentNames
                    )
                }
            }
        }

        return if (command is IReplCommandLeaf) {
            try {
                command.execute(input.map { it.value })
            } catch (e: Exception) {
                listOf(
                    "Command failed with ${e::class.simpleName ?: "Exception"}: ${e.message}"
                )
            }
        } else {
            if (nextInput != null) {
                if (command is ReplRootCommand) {
                    listOf("Unknown command '${(parentNames + nextInput.value).joinToString(" ")}'!") + command.printHelp(
                        parentNames)
                } else {
                    listOf("Unknown command '${(parentNames + command.name + nextInput.value).joinToString(" ")}'!") + command.printHelp(
                        parentNames)
                }
            } else {
                command.printHelp(parentNames)
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
                val subCommand = command.commands.find {
                    it.name == nextInput.value
                }

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
                val subCommand = command.commands.find {
                    it.name == nextToken.value
                }

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
        var input = input.trimStart()
        if (open && input.isEmpty()) {
            return listOf(Token("", 0..0))
        }

        if (!open) {
            input = input.trimEnd()
        }

        val regex = if (open) {
            """'(?:[^'\\]|\\.)*('|$)|"(?:[^"\\]|\\.)*("|$)|([^ '"\\])+""".toRegex()
        } else {
            """'(?:[^'\\]|\\.)*'|"(?:[^"\\]|\\.)*"|([^ '"\\])+""".toRegex()
        }
        var splitList = regex.findAll(input).map {
            Token(it.value, it.range)
        }.toList()
        if (open && input.endsWith(" ")) {
            splitList = splitList + Token("", input.length..input.length)
        }

        val joinedInput = splitList.joinToString(" ") {
            it.value
        }
        if (input != joinedInput) {
            println("Invalid input")
            println(splitList)
            println(input)
            println(joinedInput)
            return emptyList()
        }

        val tokenList = splitList.map {
            it.removeEscape(open)
        }

        return tokenList
    }
}
