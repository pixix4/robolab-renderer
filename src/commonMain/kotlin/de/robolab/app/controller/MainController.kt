package de.robolab.app.controller

import de.robolab.app.model.ISideBarPlottable
import de.robolab.communication.MessageManager
import de.robolab.communication.RobolabMessageProvider
import de.robolab.communication.mqtt.RobolabMqttConnection
import de.robolab.utils.ConsoleGreeter
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import de.westermann.kobserve.property.property

class MainController {

    private val selectedEntryProperty = property<ISideBarPlottable?>(null)

    private val connection = RobolabMqttConnection()
    private val messageManager = MessageManager(RobolabMessageProvider(connection))

    val applicationTitleProperty = selectedEntryProperty.nullableFlatMapBinding { it?.tabNameProperty }.mapBinding { name ->
        if (name == null) "RobolabRenderer" else "RobolabRenderer - $name"
    }

    val sideBarController = SideBarController(selectedEntryProperty, messageManager, connection)
    val canvasController = CanvasController(selectedEntryProperty)
    val toolBarController = ToolBarController(selectedEntryProperty, canvasController)
    val statusBarController = StatusBarController(canvasController)
    val infoBarController = InfoBarController(selectedEntryProperty)

    init {
        ConsoleGreeter.greet()
    }
}
