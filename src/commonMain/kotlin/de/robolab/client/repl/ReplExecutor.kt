package de.robolab.client.repl

import de.robolab.client.repl.base.IReplCommand
import de.robolab.client.repl.base.IReplCommandLeaf
import de.robolab.client.repl.base.IReplCommandNode

object ReplExecutor {

    suspend fun execute(input: String) = execute(
        ReplRootCommand,
        tokenize(input),
        emptyList()
    )

    private suspend fun execute(command: IReplCommand, input: List<String>, parentNames: List<String>): List<String> {
        val nextInput = input.firstOrNull()

        if (nextInput != null) {
            if (nextInput == "help") {
                return command.printHelp(parentNames)
            }

            if (command is IReplCommandNode) {
                val subCommand = command.commands.find {
                    it.name == nextInput
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
                command.execute(input)
            } catch (e: Exception) {
                e.printStackTrace()
                listOf(
                    "Command failed with exception: ${e.message}!"
                )
            }
        } else {
            if (nextInput != null) {
                if (command is ReplRootCommand) {
                    listOf("Unknown command '${(parentNames + nextInput).joinToString(" ")}'!") + command.printHelp(
                        parentNames)
                } else {
                    listOf("Unknown command '${(parentNames + command.name + nextInput).joinToString(" ")}'!") + command.printHelp(
                        parentNames)
                }
            } else {
                command.printHelp(parentNames)
            }
        }
    }

    private fun tokenize(input: String, open: Boolean = false): List<String> {
        val regex = if (open) {
            """'(?:[^'\\]|\\.)*('|$)|"(?:[^"\\]|\\.)*("|$)|([^ '"\\])+""".toRegex()
        } else {
            """'(?:[^'\\]|\\.)*'|"(?:[^"\\]|\\.)*"|([^ '"\\])+""".toRegex()
        }
        val splitList = regex.findAll(input).map { it.value }.toList()

        if (input != splitList.joinToString(" ")) {
            println("Invalid input")
            println(splitList)
            println(splitList.joinToString(" "))
            println(input)
            return emptyList()
        }

        return splitList.map {
            if (it.startsWith('"') && it.endsWith('"')) {
                it.removeSurrounding("\"")
            } else if (it.startsWith('\'') && it.endsWith('\'')) {
                it.removeSurrounding("'")
            } else {
                if (open) {
                    if (it.startsWith('"')) {
                        it.removePrefix("\"")
                    } else if (it.startsWith('\'')) {
                        it.removePrefix("'")
                    } else {
                        it
                    }
                } else {
                    it
                }
            }
        }
    }
}
