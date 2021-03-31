package de.robolab.client.app.controller

import de.robolab.client.utils.ContextMenu
import de.westermann.kobserve.base.ObservableValue


expect object SystemController {

    val memoryUsageProperty: ObservableValue<String>

    fun openContextMenu(menu: ContextMenu)

    val fixedRemoteUrl: String?
}
