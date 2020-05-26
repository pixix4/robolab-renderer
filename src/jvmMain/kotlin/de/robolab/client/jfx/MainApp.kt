package de.robolab.client.jfx

import de.robolab.client.jfx.style.StylesheetLoader
import de.robolab.client.jfx.style.SystemTheme.getSystemTheme
import de.robolab.client.jfx.style.SystemTheme.isSystemThemeSupported
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.utils.runAfterTimeoutInterval
import tornadofx.App


class MainApp : App(MainView::class) {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            Thread.setDefaultUncaughtExceptionHandler(ErrorHandler())

            System.setProperty("awt.useSystemAAFontSettings", "on")
            System.setProperty("jdk.gtk.version", "3")
            System.setProperty("prism.lcdtext", "false")
            System.setProperty("sun.java2d.opengl", "true")
            System.setProperty("swing.aatext", "true")
            System.setProperty("swing.crossplatformlaf", "com.sun.java.swing.plaf.gtk.GTKLookAndFeel")
            System.setProperty("swing.defaultlaf", "com.sun.java.swing.plaf.gtk.GTKLookAndFeel")

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
            StylesheetLoader.load()

            // Enable style reloading
            // Must be executed after initial theme is set.
            // Otherwise a Toolkit not initialized exception may be thrown.
            PreferenceStorage.selectedThemeProperty.onChange {
                StylesheetLoader.load()
            }

            launch(MainApp::class.java)
        }
    }
}
