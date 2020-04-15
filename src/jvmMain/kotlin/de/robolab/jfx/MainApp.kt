package de.robolab.jfx

import de.robolab.jfx.style.MainStyle
import de.robolab.utils.PreferenceStorage
import javafx.application.Platform
import javafx.util.Duration
import tornadofx.*

class MainApp : App(MainView::class, MainStyle::class) {

    companion object {

        private fun reloadStylesheets() {
            removeStylesheet(MainStyle::class)

            runLater(Duration(100.0)) {
                Platform.runLater {
                    importStylesheet(MainStyle::class)
                }
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty("prism.lcdtext", "false");

            PreferenceStorage.selectedThemeProperty.onChange {
                reloadStylesheets()
            }
            PreferenceStorage.useSystemThemeProperty.onChange {
                reloadStylesheets()
            }

            launch(MainApp::class.java)
        }
    }
}
