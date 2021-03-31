package de.robolab.client.app.controller

import com.soywiz.klock.DateTime
import de.robolab.client.app.controller.ui.*
import de.robolab.client.app.repository.DatabaseMessageStorage
import de.robolab.client.app.repository.MemoryMessageStorage
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.communication.MessageManager
import de.robolab.client.communication.RobolabMessageProvider
import de.robolab.client.communication.mqtt.RobolabMqttConnection
import de.robolab.client.utils.MqttStorage
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.utils.cache.MemoryCacheStorage
import de.robolab.client.utils.cache.PersistentCacheStorage
import de.robolab.common.utils.ConsoleGreeter
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainController(private val args: Args) {

    // Initialize utility classes
    val uiController = UiController()
    val progressController = ProgressController()
    private val mqttStorage = when (PreferenceStorage.mqttStorage) {
        MqttStorage.IN_MEMORY -> MemoryMessageStorage()
        MqttStorage.DATABASE -> DatabaseMessageStorage()
    }
    private val cacheStorage = try {
        PersistentCacheStorage()
    } catch (e: Exception) {
        MemoryCacheStorage()
    }


    // Initialize mqtt/group connect
    private val robolabMqttConnection = RobolabMqttConnection()
    private val robolabMessageProvider = RobolabMessageProvider(robolabMqttConnection)
    private val messageManager = MessageManager(robolabMessageProvider)
    private val messageRepository = MessageRepository(mqttStorage)

    // Initialize server/file connect
    private val remoteServerController = RemoteServerController()
    private val localServerController = LocalServerController()
    private val filePlanetController = FilePlanetController(
        remoteServerController,
        localServerController,
        cacheStorage,
    )

    private val connectionController = ConnectionController(
        robolabMqttConnection,
        remoteServerController
    )

    // Initialize ui controller
    val contentController = ContentController()

    val applicationTitleProperty =
        contentController.activeTabProperty.flatMapBinding {
            it.documentProperty
        }.nullableFlatMapBinding {
            it.nameProperty
        }.mapBinding { name ->
            if (name == null || name.isBlank()) APPLICATION_NAME else "$APPLICATION_NAME - $name"
        }

    val fileImportController = FileImportController(robolabMessageProvider, filePlanetController, contentController, uiController)

    val navigationBarController = NavigationBarController(
        contentController,
        messageRepository,
        messageManager,
        filePlanetController,
        uiController,
    )
    val toolBarController = ToolBarController(
        contentController,
        remoteServerController,
        uiController
    )

    val infoBarController = InfoBarController(
        contentController,
        uiController,
    )
    val statusBarController = StatusBarController(connectionController, contentController, progressController)

    init {
        ConsoleGreeter.greetClient()

        messageManager.onMessage {
            messageRepository.addMessage(it)
        }
        messageManager.onMessageList {
            messageRepository.addMessageList(it)
        }
    }

    fun finishSetup() {
        if (args.layout != null) {
            val split = args.layout.split("x")
            val rowCount = split.getOrNull(0)?.toIntOrNull()
            val colCount = split.getOrNull(1)?.toIntOrNull()
            if (rowCount != null && colCount != null) {
                contentController.content.setGridLayout(rowCount, colCount)
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
                        TODO()
                    }
                }
            }
        }
        if (args.fullscreen != null && args.fullscreen.toBoolean()) {
            uiController.fullscreenProperty.value = true
        }
        if (args.connect != null && args.connect.toBoolean()) {
            robolabMqttConnection.connectionState.onAction()
        }

        GlobalScope.launch {
            for ((filename, lastModified, producer) in args.files) {
                fileImportController.importFile(filename, lastModified, producer)
            }
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
        val files: List<ArgFile>
    )

    data class ArgFile(
        val name: String,
        val dateTime: DateTime,
        val content: suspend () -> Sequence<String>
    )
}
