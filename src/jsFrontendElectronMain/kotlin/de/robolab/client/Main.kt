package de.robolab.client

import de.robolab.client.app.controller.MainController
import de.robolab.client.app.model.file.File
import de.robolab.client.ui.initMainView
import de.robolab.common.utils.ConsoleGreeter

fun main() {
    val electronArgs = js("window.process.argv") as Array<String>
    val args = electronArgs.dropWhile { it != "--##--" }.drop(1)

    var layout = ""
    var groups = ""
    var connect = false
    var fullscreen = false
    val files = mutableListOf<String>()

    val argIterator = args.iterator()

    while (argIterator.hasNext()) {
        when (val option = argIterator.next()) {
            "--layout" -> layout = argIterator.next()
            "--groups" -> groups = argIterator.next()
            "--connect" -> connect = true
            "--fullscreen" -> fullscreen = true
            "--help" -> {
                ConsoleGreeter.greetClient()
                console.log("""
                    Usage
                        ./robolab-renderer OPTIONS ARGUMENTS
                    
                    Options:
                        --layout "3x3"       Layout of the plotter (row x col)
                        --groups "13+17+42"  Init loaded groups (Separated with '+')
                        --connect            Auto connect to mqtt server
                        --fullscreen         Start in fullscreen mode
                        
                    Arguments:
                        [files...]           Import planet or log files
                """.trimIndent())
                return
            }
            else -> files += option
        }
    }

    val f = files.map {
        val file = File(it)
        MainController.ArgFile(
            it,
            file.lastModified,
            suspend {
                file.readText().splitToSequence("\n")
            }
        )
    }

    val mainArgs = MainController.Args(
        layout,
        groups,
        fullscreen.toString(),
        connect.toString(),
        f
    )


    initMainView(mainArgs)
}
