package de.robolab.client.app.controller

import de.robolab.client.app.model.group.GroupLiveAttemptPlanetDocument
import de.robolab.client.app.repository.DatabaseMessageStorage
import de.robolab.client.app.repository.MemoryMessageStorage
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.communication.MessageManager
import de.robolab.client.communication.RobolabMessageProvider
import de.robolab.client.communication.mqtt.RobolabMqttConnection
import de.robolab.client.utils.MqttStorage
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.utils.runAfterTimeout
import de.robolab.common.utils.ConsoleGreeter
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainController(private val args: Args) {

    private val connection = RobolabMqttConnection()

    val tabController = TabController(connection)
    val uiController = UiController(tabController)

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

        tabController.fullscreenProperty.onChange {
            if (tabController.fullscreenProperty.value) {
                tabController.activeTabProperty.value?.plotterManager?.hideHighlight()
            }
        }
    }

    fun finishSetup() {
        tabController.openNewTab()

        if (args.layout != null) {
            val split = args.layout.split("x")
            val rowCount = split.getOrNull(0)?.toIntOrNull()
            val colCount = split.getOrNull(1)?.toIntOrNull()
            if (rowCount != null && colCount != null) {
                tabController.activeTabProperty.value?.plotterManager?.setGridLayout(rowCount, colCount)
            }
        }
        if (args.groups != null) {
            val groups = args.groups.split("+")
            GlobalScope.launch {
                for ((index, groupName) in groups.withIndex()) {
                    if (groupName.isBlank()) continue
                    val group = messageRepository.createEmptyGroup(groupName) ?: continue
                    val attempt = messageRepository.getLatestAttempt(group.groupId)

                    withContext(Dispatchers.Main) {
                        tabController.activeTabProperty.value?.plotterManager?.setActive(index)

                        tabController.open(
                            GroupLiveAttemptPlanetDocument(
                                group,
                                attempt,
                                messageRepository,
                                messageManager,
                                navigationBarController.groupPlanetProperty.planetProvider
                            ), false
                        )
                    }
                }
                withContext(Dispatchers.Main) {
                    tabController.activeTabProperty.value?.plotterManager?.hideHighlight()
                }
            }
        }
        if (args.fullscreen != null && args.fullscreen.toBoolean()) {
            tabController.fullscreenProperty.value = true
        }
        if (args.connect != null && args.connect.toBoolean()) {
            connection.connectionState.onAction()
        }

        runAfterTimeout(1000) {
            tabController.activeTabProperty.value?.plotterManager?.requestRedraw = true
        }
    }

    companion object {
        const val APPLICATION_NAME = "RobolabRenderer"
    }

    data class Args(
        val layout: String? = null,
        val groups: String? = null,
        val fullscreen: String? = null,
        val connect: String? = null,
    )
}
