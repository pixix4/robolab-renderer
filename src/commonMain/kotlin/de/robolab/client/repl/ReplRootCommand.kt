package de.robolab.client.repl

import de.robolab.client.repl.base.IReplCommand
import de.robolab.client.repl.base.IReplCommandNode
import de.robolab.client.repl.base.buildList
import de.robolab.common.utils.ConsoleGreeter

object ReplRootCommand: ReplCommandNode("robolab", "RoboLab Renderer repl") {
    override fun printHelp(parentNames: List<String>): List<String> {
        return buildList {
            addAll(ConsoleGreeter.appLogo.split("\n"))
            add(ConsoleGreeter.appClientCreators)
            add("")
            add("Commands:")

            val nameLength = helpCommandDescriptions.maxOf { it.first.length }
            for ((commandName, commandDescription) in helpCommandDescriptions) {
                val padding = " ".repeat(nameLength - commandName.length)
                add("    ${commandName}: $padding$commandDescription")
            }
        }
    }

    private fun buildDebugTree(command: IReplCommand): List<String> {
        return if (command is IReplCommandNode) {
            listOf("${command.name}: ${command.description}") + command.commands.sortedBy { it.name }.flatMap { c ->
                buildDebugTree(c).map { "    $it" }
            }
        } else {
            listOf("${command.name}: ${command.description}")
        }
    }

    init {
        this.node("debug", "Debug related commands") {
            action("tree", "Print command hierarchy") { ->
                ReplRootCommand.commands.sortedBy { it.name }.flatMap(this@ReplRootCommand::buildDebugTree)
            }
        }
    }
}
