package de.robolab.client.repl.commands

import de.robolab.client.repl.base.*

object TreeCommand : ReplManagementCommand(
    "tree",
    "Print command hierarchy of this node",
) {

    override fun execute(commandChain: List<CommandBinding<*>>, context: IReplExecutionContext) {
        printTreeCommand(
            commandChain.lastOrNull()?.command ?: RootCommand,
            context,
            0,
        )
    }

    private fun printTreeCommand(command: ReplBindableCommand<*>, context: IReplExecutionContext, depth: Int) {
        when (command) {
            is RootCommand -> printTreeRootCommand(context)
            is ReplBindableLeafCommand -> printTreeLeafCommand(command, context, depth)
            is ReplBindableNodeCommand -> printTreeNodeCommand(command, context, depth)
            is ReplManagementCommand -> printTreeManagementCommand(command, context, depth)
        }
    }

    private fun printTreeLeafCommand(command: ReplBindableLeafCommand<*>, context: IReplExecutionContext, depth: Int) {
        context.write("    ".repeat(depth))

        context.write(command.name, ReplColor.CYAN)
        context.write(": ")
        context.writeln(command.description, ReplColor.GREY)
    }

    private fun printTreeManagementCommand(command: ReplManagementCommand, context: IReplExecutionContext, depth: Int) {
        context.write("    ".repeat(depth))

        context.write(command.name, ReplColor.YELLOW)
        context.write(": ")
        context.writeln(command.description, ReplColor.GREY)
    }

    private fun printTreeNodeCommand(
        command: ReplBindableNodeCommand<*>,
        context: IReplExecutionContext,
        depth: Int,
    ) {
        context.write("    ".repeat(depth))

        context.write(command.name, ReplColor.BLUE)
        context.write(": ")
        context.writeln(command.description, ReplColor.GREY)

        val sortedCommandList = command.children.sortedWith(compareBy<ReplBindableCommand<*>> {
            when (it) {
                is ReplManagementCommand -> 1
                is ReplBindableLeafCommand -> 2
                is ReplBindableNodeCommand -> 3
            }
        }.thenBy { it.name })

        for (subCommand in sortedCommandList) {
            printTreeCommand(subCommand, context, depth + 1)
        }
    }

    private fun printTreeRootCommand(
        context: IReplExecutionContext,
    ) {
        val sortedCommandList = RootCommand.children.sortedWith(compareBy<ReplBindableCommand<*>> {
            when (it) {
                is ReplManagementCommand -> 1
                is ReplBindableLeafCommand -> 2
                is ReplBindableNodeCommand -> 3
            }
        }.thenBy { it.name })

        for (subCommand in sortedCommandList) {
            printTreeCommand(subCommand, context, 0)
        }
    }
}
