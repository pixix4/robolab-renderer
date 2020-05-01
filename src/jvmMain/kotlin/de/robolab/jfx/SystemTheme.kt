package de.robolab.jfx

import de.robolab.theme.Theme
import de.robolab.utils.PreferenceStorage
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

    private fun isWinDarkMode(): Boolean {
        return try {
            val proc = Runtime.getRuntime().exec(arrayOf("reg", "query", "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize", "/v", "AppsUseLightTheme"))
            proc.waitFor(100, TimeUnit.MILLISECONDS)
            proc.exitValue() == 0 && String(proc.inputStream.readBytes()).contains("0x0", true)
        } catch (ex: Exception) {
            false
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
            proc.exitValue() == 0 && String(proc.inputStream.readBytes()).contains("dark", true)
        } catch (ex: Exception) {
            false
        }
    }

    private val linuxThemeFile by lazy { System.getProperty("user.home", "") + "/.theme" }
    private fun isLinuxDarkMode(): Boolean {
        return try {
            val content = Files.readString(Paths.get(linuxThemeFile))
            content.contains("dark", true)
        } catch (ex: Exception) {
            false
        }
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
