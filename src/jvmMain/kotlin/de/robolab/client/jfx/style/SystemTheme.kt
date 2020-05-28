package de.robolab.client.jfx.style

import de.robolab.client.theme.Theme
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.jfx.utils.SystemOs
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

object SystemTheme {

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

    val isSystemThemeSupported = when (SystemOs.os) {
        SystemOs.OSType.Windows -> true
        SystemOs.OSType.MacOS -> true
        SystemOs.OSType.Linux -> true
        SystemOs.OSType.Other -> false
    }

    fun getSystemTheme(): Theme {
        val isDarkMode = when (SystemOs.os) {
            SystemOs.OSType.Windows -> isWinDarkMode()
            SystemOs.OSType.MacOS -> isMacDarkMode()
            SystemOs.OSType.Linux -> isLinuxDarkMode()
            else -> false
        }

        return PreferenceStorage.selectedTheme.getThemeByMode(isDarkMode)
    }
}
