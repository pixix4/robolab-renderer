package de.robolab.client.repl.commands

import de.robolab.client.repl.base.*
import de.robolab.client.repl.escapeIfNecessary
import de.robolab.common.utils.ConsoleGreeter

object HelpCommand : ReplManagementCommand(
    "help",
    "Print the help message of a command",
) {

    override fun execute(commandChain: List<CommandBinding<*>>, context: IReplExecutionContext) {
        when (val command = commandChain.lastOrNull()?.command ?: RootCommand) {
            is RootCommand -> printHelpRootCommand(context)
            is ReplBindableLeafCommand -> printHelpLeafCommand(command, commandChain, context)
            is ReplBindableNodeCommand -> printHelpNodeCommand(command, commandChain, context)
            is ReplManagementCommand -> printHelpManagementCommand(command, commandChain, context)
        }
    }

    private fun printHelpLeafCommand(
        command: ReplBindableLeafCommand<*>,
        commandChain: List<CommandBinding<*>>,
        context: IReplExecutionContext,
    ) {
        val params = command.parameters.joinToString(" ") {
            val s = "${it.name}: ${it.type.name}"

            when (it) {
                is ReplCommandDefaultParameterDescriptor -> "<$s>"
                is ReplCommandOptionalParameterDescriptor -> "[$s]"
                is ReplCommandVarargParameterDescriptor -> "[$s ...]"
            }
        }

        context.writeln((commandChain.joinToString(" ") { it.name }))
        context.writeln("")
        context.writeln("Description:")
        context.writeln("    ${command.description}")
        context.writeln("")
        context.writeln("Usage:")
        context.writeln("    $name $params")

        for (parameter in command.parameters
            .asSequence()
            .map { it.type }
            .distinct()
            .sortedBy { it.name }) {
            context.writeln()
            if (parameter.pattern.contains(' ')) {
                context.writeln("${parameter.name}: \"${parameter.pattern}\"")
            } else {
                context.writeln("${parameter.name}: ${parameter.pattern}")
            }
            context.writeln("    ${parameter.description}")

            if (parameter.example.isNotEmpty()) {
                context.writeln("    Example: \"${parameter.example.first().escapeIfNecessary()}\"")
            }
            for (e in parameter.example.drop(1)) {
                context.writeln("             \"${parameter.example.first().escapeIfNecessary()}\"")
            }
        }
    }


    private fun printHelpManagementCommand(
        command: ReplManagementCommand,
        commandChain: List<CommandBinding<*>>,
        context: IReplExecutionContext,
    ) {
        context.writeln((commandChain.joinToString(" ") { it.name }))
        context.writeln("")
        context.writeln("Description:")
        context.writeln("    ${command.description}")
    }

    private fun printHelpNodeCommand(
        command: ReplBindableNodeCommand<*>,
        commandChain: List<CommandBinding<*>>,
        context: IReplExecutionContext,
    ) {
        context.writeln((commandChain.joinToString(" ") { it.name }))
        context.writeln()
        context.writeln("Description:")
        context.writeln("    ${command.description}")
        context.writeln()
        context.writeln("Commands:")

        val helpCommandDescriptions = command.children
            .map {
                Triple(it.name, it.description, when (it) {
                    is ReplBindableLeafCommand<*> -> ReplColor.CYAN
                    is ReplBindableNodeCommand<*> -> ReplColor.BLUE
                    is ReplManagementCommand -> ReplColor.YELLOW
                })
            }.sortedWith(compareBy<Triple<String, String, ReplColor>> {
                when (it.third ) {
                    ReplColor.YELLOW -> 1
                    ReplColor.CYAN -> 2
                    ReplColor.BLUE -> 3
                    else -> 10
                }
            }.thenBy { it.first })

        val nameLength = helpCommandDescriptions.maxOf { it.first.length }
        for ((commandName, commandDescription, color) in helpCommandDescriptions) {
            val padding = " ".repeat(nameLength - commandName.length)

            context.write("    ")
            context.write(commandName, color)
            context.write(": $padding")
            context.writeln(commandDescription, ReplColor.GREY)
        }
    }

    private fun printHelpRootCommand(
        context: IReplExecutionContext,
    ) {
        context.writeln(ConsoleGreeter.appLogo)
        context.writeln(ConsoleGreeter.appClientCreators, ReplColor.GREY)
        context.writeln()
        context.writeln("Commands:")

        val helpCommandDescriptions = RootCommand.children
            .map {
                Triple(it.name, it.description, when (it) {
                    is ReplBindableLeafCommand<*> -> ReplColor.CYAN
                    is ReplBindableNodeCommand<*> -> ReplColor.BLUE
                    is ReplManagementCommand -> ReplColor.YELLOW
                })
            }.sortedWith(compareBy<Triple<String, String, ReplColor>> {
                when (it.third ) {
                    ReplColor.YELLOW -> 1
                    ReplColor.CYAN -> 2
                    ReplColor.BLUE -> 3
                    else -> 10
                }
            }.thenBy { it.first })

        val nameLength = helpCommandDescriptions.maxOf { it.first.length }
        for ((commandName, commandDescription, color) in helpCommandDescriptions) {
            val padding = " ".repeat(nameLength - commandName.length)

            context.write("    ")
            context.write(commandName, color)
            context.write(": $padding")
            context.writeln(commandDescription, ReplColor.GREY)
        }
    }
}
