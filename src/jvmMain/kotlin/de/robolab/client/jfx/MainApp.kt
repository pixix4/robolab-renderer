package de.robolab.client.jfx

import com.sun.javafx.application.LauncherImpl
import de.robolab.client.jfx.style.StylesheetLoader
import de.robolab.client.jfx.style.SystemTheme.getSystemTheme
import de.robolab.client.jfx.style.SystemTheme.isSystemThemeSupported
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.utils.runAfterTimeoutInterval
import de.robolab.common.utils.ConfigFile
import de.robolab.common.utils.Logger
import de.westermann.kobserve.event.now
import tornadofx.App
import java.nio.file.Paths
import kotlin.concurrent.thread


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

            for (arg in args) {
                try {
                    val path = Paths.get(arg)
                    ConfigFile.localPath = path
                } catch (e: Exception) {
                }
            }

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

            setupMemoryDebugThread()

            LauncherImpl.launchApplication(MainApp::class.java, MainAppPreloader::class.java, args)
        }
    }
}

fun setupMemoryDebugThread() {
    fun createThread() {
        thread(name = "MemoryDebugThread") {
            val logger = Logger("Memory logger")
            while (PreferenceStorage.debugMode) {
                Thread.sleep(1_000)
                logger.i {
                    val bytes = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
                    val mb = bytes / 1024 / 1024
                    "Mem: ${mb}MB"
                }
            }
        }
    }

    PreferenceStorage.debugModeProperty.onChange.now {
        if (PreferenceStorage.debugMode) {
            createThread()
        }
    }
}