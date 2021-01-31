 package de.robolab.client.app.controller

import de.robolab.client.utils.electron

actual fun getMemoryUsageString(): String {
    val info = electron?.getMemoryInfo() ?: return ""

    return (info.total / 1024).toString() + "MB"
}
