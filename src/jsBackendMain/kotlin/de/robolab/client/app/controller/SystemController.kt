package de.robolab.client.app.controller

import de.robolab.client.utils.ContextMenu
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.property.constObservable

actual object SystemController {
    actual val memoryUsageProperty: ObservableValue<String> = constObservable("")

    actual fun openContextMenu(menu: ContextMenu) {
    }

}