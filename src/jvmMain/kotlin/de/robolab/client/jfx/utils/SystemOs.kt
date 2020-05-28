package de.robolab.client.jfx.utils

import java.lang.System

object SystemOs {

    enum class OSType {
        Windows, MacOS, Linux, Other
    }

    val os by lazy {
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
}