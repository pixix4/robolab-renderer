package de.robolab.client.app.controller

import de.robolab.client.utils.ContextMenu
import de.robolab.client.utils.electron
import de.robolab.client.utils.runAfterTimeoutInterval
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.property

actual object SystemController {

    private val memoryUsageProperty2 = property("")
    actual val memoryUsageProperty: ObservableValue<String> = memoryUsageProperty2

    actual fun openContextMenu(menu: ContextMenu) {
        electron?.menu(menu)
    }

    actual val fixedRemoteUrl: String? = null

    actual val isDesktop: Boolean = true

    private fun getMemoryUsageString(): String {
        val info = electron?.getMemoryInfo() ?: return ""

        return (info.total / 1024).toString() + "MB"
    }

    init {
        runAfterTimeoutInterval(1000) {
            memoryUsageProperty2.value = getMemoryUsageString()
        }
    }
}
