package de.robolab.client

import com.github.ajalt.clikt.core.subcommands

object Launcher {
    @JvmStatic
    fun main(args: Array<String>) = CommandLineParser().subcommands(
        CommandLineParser.Export(),
        CommandLineParser.Update(),
    ).main(args)
}
