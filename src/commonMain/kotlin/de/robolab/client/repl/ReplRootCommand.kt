package de.robolab.client.repl

import de.robolab.client.repl.base.*
import de.robolab.common.utils.ConsoleGreeter

object ReplRootCommand : ReplCommandNode("robolab", "RoboLab Renderer repl") {
    override fun printHelp(output: IReplOutput, parentNames: List<String>) {
        output.writeln(ConsoleGreeter.appLogo)
        output.writeln(ConsoleGreeter.appClientCreators, ReplColor.GREY)
        output.writeln("")
        output.writeln("Commands:")

        val nameLength = helpCommandDescriptions.maxOf { it.first.length }
        for ((commandName, commandDescription, color) in helpCommandDescriptions) {
            val padding = " ".repeat(nameLength - commandName.length)

            output.write("    ")
            output.write(commandName, color)
            output.write(": $padding")
            output.writeln(commandDescription, ReplColor.GREY)
        }
    }

    private fun printDebugTree(output: IReplOutput, command: IReplCommand, depth: Int) {
        output.write("    ".repeat(depth))

        if (command is IReplCommandNode) {
            output.write(command.name, ReplColor.BLUE)
            output.write(": ")
            output.writeln(command.description, ReplColor.GREY)

            for (subCommand in command.commands.sortedBy { it.name }) {
                printDebugTree(output, subCommand, depth + 1)
            }
        } else if (command is IReplCommandLeaf) {
            output.write(command.name, ReplColor.CYAN)
            output.write(": ")
            output.writeln(command.description, ReplColor.GREY)
        }
    }

    init {
        this.node("debug", "Debug related commands") {
            action("tree", "Print command hierarchy") { output ->
                for (command in ReplRootCommand.commands.sortedBy { it.name }) {
                    printDebugTree(output, command, 0)
                }
            }
        }
    }
}
