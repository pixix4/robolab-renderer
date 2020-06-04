package de.robolab.client.app.controller

import de.robolab.client.app.model.INavigationBarPlottable
import de.robolab.client.communication.MessageManager
import de.robolab.client.communication.RobolabMessageProvider
import de.robolab.client.communication.mqtt.RobolabMqttConnection
import de.robolab.common.utils.ConsoleGreeter
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import de.westermann.kobserve.property.property

class MainController {

    private val selectedEntryProperty = property<INavigationBarPlottable?>(null)

    private val connection = RobolabMqttConnection()
    private val robolabMessageProvider = RobolabMessageProvider(connection)
    private val messageManager = MessageManager(robolabMessageProvider)

    val applicationTitleProperty =
        selectedEntryProperty.nullableFlatMapBinding { it?.tabNameProperty }.mapBinding { name ->
            if (name == null) "RobolabRenderer" else "RobolabRenderer - $name"
        }

    val canvasController = CanvasController(selectedEntryProperty)
    val sideBarController = NavigationBarController(selectedEntryProperty, messageManager, connection, canvasController)
    val toolBarController = ToolBarController(selectedEntryProperty, canvasController)
    val statusBarController = StatusBarController(canvasController)
    val infoBarController = InfoBarController(selectedEntryProperty)

    init {
        ConsoleGreeter.greet()
    }
    
    fun importLogFile(log: String) {
        robolabMessageProvider.importMqttLog(log)
    }
}
