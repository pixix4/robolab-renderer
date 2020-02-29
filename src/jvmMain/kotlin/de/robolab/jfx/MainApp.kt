package de.robolab.jfx

import tornadofx.*

/**
 * @author leon
 */
class MainApp : App(MainView::class) {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty("prism.lcdtext", "false");

            launch(MainApp::class.java)
        }
    }
}
