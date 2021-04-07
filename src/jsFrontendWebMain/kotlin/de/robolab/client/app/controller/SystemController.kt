package de.robolab.client.app.controller

import de.robolab.client.ui.views.utils.ContextMenuView
import de.robolab.client.utils.ContextMenu
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.constObservable
import kotlinx.browser.window

actual object SystemController {
    actual val memoryUsageProperty: ObservableValue<String> = constObservable("")

    actual fun openContextMenu(menu: ContextMenu) {
        ContextMenuView.open(menu)
    }

    @Suppress("RedundantNullableReturnType")
    actual val fixedRemoteUrl: String? = window.location.href

    actual val isDesktop: Boolean = false
}
