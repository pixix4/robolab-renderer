package de.robolab.client.jfx

import com.sun.javafx.application.LauncherImpl
import de.robolab.client.jfx.style.StylesheetLoader
import de.robolab.client.jfx.style.SystemTheme.getSystemTheme
import de.robolab.client.jfx.style.SystemTheme.isSystemThemeSupported
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.utils.runAfterTimeoutInterval
import de.robolab.common.utils.BuildInformation
import de.robolab.common.utils.ConfigFile
import de.robolab.common.utils.ConsoleGreeter
import de.robolab.common.utils.Logger
import de.westermann.kobserve.event.now
import javafx.stage.Stage
import javafx.stage.StageStyle
import tornadofx.App
import tornadofx.NoPrimaryViewSpecified
import tornadofx.UIComponent
import java.nio.file.Paths
import kotlin.concurrent.thread
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

        super.start(stage)

        stage.toFront()
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            Thread.setDefaultUncaughtExceptionHandler(ErrorHandler())
            ConsoleGreeter.greetClient()

            System.setProperty("awt.useSystemAAFontSettings", "on")
            System.setProperty("jdk.gtk.version", "3")
            System.setProperty("prism.lcdtext", "false")
            System.setProperty("sun.java2d.opengl", "true")
            System.setProperty("swing.aatext", "true")
            System.setProperty("swing.crossplatformlaf", "com.sun.java.swing.plaf.gtk.GTKLookAndFeel")
            System.setProperty("swing.defaultlaf", "com.sun.java.swing.plaf.gtk.GTKLookAndFeel")

            println("buildTime: ${BuildInformation.buildTime}")
            println("buildTools: ${BuildInformation.buildTools}")
            println("buildSystem: ${BuildInformation.buildSystem}")
            println("buildUser: ${BuildInformation.buildUser}")
            println("vcsBranch: ${BuildInformation.vcsBranch}")
            println("vcsCommit: ${BuildInformation.vcsCommit}")
            println("vcsTag: ${BuildInformation.vcsTag}")
            println("vcsLastTag: ${BuildInformation.vcsLastTag}")
            println("vcsDirty: ${BuildInformation.vcsDirty}")
            println("versionClient: ${BuildInformation.versionClient}")
            println("versionServer: ${BuildInformation.versionServer}")

            println(BuildInformation.versionClient < BuildInformation.versionServer)
            println(BuildInformation.versionClient > BuildInformation.versionServer)
            println(BuildInformation.versionClient == BuildInformation.versionServer)

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