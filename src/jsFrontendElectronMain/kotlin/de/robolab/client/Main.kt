package de.robolab.client

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import de.robolab.client.app.controller.MainController
import de.robolab.client.ui.initMainView

class App : CliktCommand() {

    private val layout by option("--layout", help = "Layout of the plotter (row x col)")
    private val groups by option("--groups", help = "Init loaded groups (Separated with '+')")
    private val connect by option("--connect").flag(default = false)
    private val fullscreen by option("--fullscreen").flag(default = false)

    override fun run() {
        val args = MainController.Args(
            layout,
            groups,
            fullscreen.toString(),
            connect.toString()
        )

        initMainView(args)
    }


}

fun main() {
    val electronArgs = js("window.process.argv") as Array<String>
    console.log(electronArgs)
    val args = electronArgs.dropWhile { it != "--##--" }.drop(1)
    console.log(args)
    App().main(args)
}
