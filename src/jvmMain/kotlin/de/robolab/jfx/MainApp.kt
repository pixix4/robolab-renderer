package de.robolab.jfx

import de.robolab.jfx.style.MainStyle
import tornadofx.*

class MainApp : App(MainView::class, MainStyle::class) {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty("prism.lcdtext", "false");

            launch(MainApp::class.java)
        }
    }
}
