package de.robolab.client.app.controller

import de.robolab.client.app.repository.DatabaseMessageStorage
import de.robolab.client.app.repository.MemoryMessageStorage
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.communication.MessageManager
import de.robolab.client.communication.RobolabMessageProvider
import de.robolab.client.communication.mqtt.RobolabMqttConnection
import de.robolab.client.utils.MqttStorage
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.utils.ConsoleGreeter
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding

class MainController {

    val tabController = TabController()
    val uiController = UiController(tabController)

    private val connection = RobolabMqttConnection()
    val robolabMessageProvider = RobolabMessageProvider(connection)
    private val messageManager = MessageManager(robolabMessageProvider)

    val applicationTitleProperty =
        tabController.activeTabProperty.nullableFlatMapBinding { it?.nameProperty }.mapBinding { name ->
            if (name == null || name.isBlank()) APPLICATION_NAME else "$APPLICATION_NAME - $name"
        }

    val fileImportController = FileImportController(robolabMessageProvider)

    val mqttStorage = when (PreferenceStorage.mqttStorage) {
        MqttStorage.IN_MEMORY -> MemoryMessageStorage()
        MqttStorage.DATABASE -> DatabaseMessageStorage()
    }
    val messageRepository = MessageRepository(mqttStorage)

    val canvasController = CanvasController(tabController.activeTabProperty)
    val navigationBarController = NavigationBarController(
        tabController,
        messageRepository,
        messageManager,
    )
    val toolBarController = ToolBarController(
        tabController.activeTabProperty,
        tabController.activeDocumentProperty,
        canvasController,
        uiController,
        navigationBarController.filePlanetProvider
    )
    val statusBarController = StatusBarController(canvasController, connection)
    val infoBarController = InfoBarController(tabController.activeDocumentProperty)

    init {
        ConsoleGreeter.greetClient()

        messageManager.onMessage {
            messageRepository.addMessage(it)
        }
        messageManager.onMessageList {
            messageRepository.addMessageList(it)
        }
    }

    companion object {
        const val APPLICATION_NAME = "RobolabRenderer"
    }
}
