package de.robolab.jfx

import de.robolab.jfx.style.MainStyle
import de.robolab.renderer.theme.Theme
import de.robolab.utils.PreferenceStorage
import javafx.application.Platform
import javafx.util.Duration
import tornadofx.*

import java.util.concurrent.TimeUnit




class MainApp : App(MainView::class) {

    companion object {

        private fun reloadStylesheets() {

            removeStylesheet(MainStyle::class)

            runLater(Duration(100.0)) {
                Platform.runLater {
                    importStylesheet(MainStyle::class)
                }
            }
        }

        /**
         * https://stackoverflow.com/questions/33477294/menubar-icon-for-dark-mode-on-os-x-in-java
         */
        private fun isMacDarkMode(): Boolean {
            return try {
                // check for exit status only. Once there are more modes than "dark" and "default", we might need to analyze string contents..
                val proc = Runtime.getRuntime().exec(arrayOf("defaults", "read", "-g", "AppleInterfaceStyle"))
                proc.waitFor(100, TimeUnit.MILLISECONDS)
                proc.exitValue() == 0
            } catch (ex: Exception) {
                false
            }
        }

        private fun getSystemTheme(): Theme {
            val isDarkMode =  isMacDarkMode()

            if (isDarkMode) {
                return Theme.DARK
            }

            return Theme.LIGHT
        }

        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty("prism.lcdtext", "false");

            PreferenceStorage.selectedThemeProperty.onChange {
                reloadStylesheets()
            }
            PreferenceStorage.useSystemThemeProperty.onChange {
                if (PreferenceStorage.useSystemTheme) {
                    PreferenceStorage.selectedTheme = getSystemTheme()
                }
            }
            if (PreferenceStorage.useSystemTheme) {
                PreferenceStorage.selectedTheme = getSystemTheme()
            }

            importStylesheet(MainStyle::class)

            launch(MainApp::class.java)
        }
    }
}
