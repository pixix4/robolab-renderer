package de.robolab.client.jfx.style

import de.robolab.client.theme.Theme
import de.robolab.client.utils.PreferenceStorage
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

object SystemTheme {

    private enum class OSType {
        Windows, MacOS, Linux, Other
    }

    private val os by lazy {
        val os = System.getProperty("os.name", "generic").toLowerCase()
        when {
            os.indexOf("mac") >= 0 || os.indexOf("darwin") >= 0 -> {
                OSType.MacOS
            }
            os.indexOf("win") >= 0 -> {
                OSType.Windows
            }
            os.indexOf("nux") >= 0 -> {
                OSType.Linux
            }
            else -> {
                OSType.Other
            }
        }
    }

    private fun runCommand(cmd: Array<String>): Pair<Int, String>? {
        return try {
            val proc = Runtime.getRuntime().exec(cmd)
            proc.waitFor(100, TimeUnit.MILLISECONDS)
            proc.exitValue() to String(proc.inputStream.readBytes())
        } catch (ex: Exception) {
            null
        }
    }

    private fun isWinDarkMode(): Boolean {
        val (exitStatus, output) = runCommand(arrayOf(
                "reg",
                "query",
                "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                "/v",
                "AppsUseLightTheme"
        )) ?: return false
        return exitStatus == 0 && output.contains("0x0", true)
    }

    /**
     * https://stackoverflow.com/questions/33477294/menubar-icon-for-dark-mode-on-os-x-in-java
     */
    private fun isMacDarkMode(): Boolean {
        val (exitStatus, output) = runCommand(arrayOf(
                "defaults",
                "read",
                "-g",
                "AppleInterfaceStyle"
        )) ?: return false
        return exitStatus == 0 && output.contains("dark", true)
    }

    private val linuxThemeFile by lazy { System.getProperty("user.home", "") + "/.theme" }
    private fun isLinuxDarkMode(): Boolean {
        try {
            val content = Files.readString(Paths.get(linuxThemeFile))
            if (content.contains("dark", true)) {
                return true
            }
        } catch (ex: Exception) {
        }

        val (exitStatus, output) = runCommand(arrayOf(
                "gsettings",
                "get",
                "org.gnome.desktop.interface",
                "gtk-theme"
        )) ?: return false
        return exitStatus == 0 && output.contains("dark", true)
    }

    val isSystemThemeSupported = when (os) {
        OSType.Windows -> true
        OSType.MacOS -> true
        OSType.Linux -> true
        OSType.Other -> false
    }

    fun getSystemTheme(): Theme {
        val isDarkMode = when (os) {
            OSType.Windows -> isWinDarkMode()
            OSType.MacOS -> isMacDarkMode()
            OSType.Linux -> isLinuxDarkMode()
            else -> false
        }

        return PreferenceStorage.selectedTheme.getThemeByMode(isDarkMode)
    }
}
