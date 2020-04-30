package de.robolab.jfx

import de.robolab.jfx.SystemTheme.getSystemTheme
import de.robolab.jfx.SystemTheme.isSystemThemeSupported
import de.robolab.jfx.style.MainStyle
import de.robolab.utils.PreferenceStorage
import de.robolab.utils.runAfterTimeoutInterval
import javafx.util.Duration
import tornadofx.*


class MainApp : App(MainView::class) {

    companion object {

        private fun reloadStylesheets() {

            removeStylesheet(MainStyle::class)

            // Wait for removal of all references to MainStyle
            runLater {
                System.gc()

                // Wait for gc() to clear the current MainStyle instance
                runLater(Duration(100.0)) {
                    importStylesheet(MainStyle::class)
                }
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty("prism.lcdtext", "false");

            PreferenceStorage.useSystemThemeProperty.onChange {
                if (PreferenceStorage.useSystemTheme) {
                    PreferenceStorage.selectedTheme = getSystemTheme()
                }
            }
            if (PreferenceStorage.useSystemTheme) {
                PreferenceStorage.selectedTheme = getSystemTheme()
            }
            if (isSystemThemeSupported) {
                runAfterTimeoutInterval(5000) {
                    // Frequently check system theme (5sec should not have a performance impact)
                    if (PreferenceStorage.useSystemTheme) {
                        PreferenceStorage.selectedTheme = getSystemTheme()
                    }
                }
            }

            // Initial loading of stylesheets
            importStylesheet(MainStyle::class)

            // Enable style reloading
            // Must be executed after initial theme is set.
            // Otherwise a Toolkit not initialized exception may be thrown.
            PreferenceStorage.selectedThemeProperty.onChange {
                reloadStylesheets()
            }

            launch(MainApp::class.java)
        }
    }
}
