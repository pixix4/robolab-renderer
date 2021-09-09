package de.robolab.client.repl

import de.robolab.client.repl.base.buildList

object ReplRootCommand: ReplCommandNode("robolab", "RoboLab Renderer repl") {
    override fun printHelp(parentNames: List<String>): List<String> {
        return buildList {
            add(description)
            add("")
            add("Commands:")
            for (command in commands) {
                add("    ${command.name}: ${command.description}")
            }
        }
    }
}
