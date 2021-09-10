package de.robolab.client.repl

import de.robolab.client.repl.base.buildList

object ReplRootCommand: ReplCommandNode("robolab", "RoboLab Renderer repl") {
    override fun printHelp(parentNames: List<String>): List<String> {
        return buildList {
            add(description)
            add("")
            add("Commands:")

            val nameLength = helpCommandDescriptions.maxOf { it.first.length }
            for ((commandName, commandDescription) in helpCommandDescriptions) {
                val padding = " ".repeat(nameLength - commandName.length)
                add("    ${commandName}: $padding$commandDescription")
            }
        }
    }
}
