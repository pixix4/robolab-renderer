package de.robolab.client.ui

import com.sun.javafx.application.LauncherImpl
import de.robolab.client.ui.dialog.UpdateDialog
import de.robolab.client.ui.style.StylesheetLoader
import de.robolab.client.ui.style.SystemTheme.getSystemTheme
import de.robolab.client.ui.style.SystemTheme.isSystemThemeSupported
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.utils.UpdateChannel
import de.robolab.client.utils.runAfterTimeoutInterval
import de.robolab.common.utils.ConsoleGreeter
import javafx.stage.Stage
import javafx.stage.StageStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.App
import tornadofx.NoPrimaryViewSpecified
import tornadofx.UIComponent
import kotlin.reflect.KClass


class MainApp : App(NoPrimaryViewSpecified::class) {

    private val isFirstStart = PreferenceStorage.firstStart

    override val primaryView: KClass<out UIComponent>
        get() = if (isFirstStart) SetupView::class else MainView::class

    override fun start(stage: Stage) {
        if (isFirstStart) {
            stage.initStyle(StageStyle.UTILITY)
            stage.isResizable = false
            stage.sizeToScene()
        } else {
            stage.initStyle(StageStyle.DECORATED)
        }

        stage.properties["params"] = parameters.raw

        super.start(stage)

        stage.toFront()
    }

    companion object {

        fun setupSystemProperties() {
            System.setProperty("awt.useSystemAAFontSettings", "on")
            System.setProperty("jdk.gtk.version", "3")
            System.setProperty("prism.lcdtext", "false")
            System.setProperty("sun.java2d.opengl", "true")
            System.setProperty("swing.aatext", "true")
            System.setProperty("swing.crossplatformlaf", "com.sun.java.swing.plaf.gtk.GTKLookAndFeel")
            System.setProperty("swing.defaultlaf", "com.sun.java.swing.plaf.gtk.GTKLookAndFeel")
        }

        fun setupTheme() {
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

        }

        @JvmStatic
        fun main(args: Array<String>) {
            Thread.setDefaultUncaughtExceptionHandler(ErrorHandler())
            ConsoleGreeter.greetClient()

            setupSystemProperties()

            runAfterTimeoutInterval(60000) {
                if (PreferenceStorage.autoUpdateChannel != UpdateChannel.NEVER) {
                    GlobalScope.launch(Dispatchers.IO) {
                        try {
                            UpdateDialog.checkUpdate()
                        } catch (e: Exception) {
                        }
                    }
                }
            }

            setupTheme()

            LauncherImpl.launchApplication(MainApp::class.java, MainAppPreloader::class.java, args)
        }
    }
}
