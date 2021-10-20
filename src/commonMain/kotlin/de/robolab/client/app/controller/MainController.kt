package de.robolab.client.app.controller

import de.robolab.client.app.controller.ui.*
import de.robolab.client.app.model.group.GroupLiveAttemptPlanetDocument
import de.robolab.client.app.repository.DatabaseMessageStorage
import de.robolab.client.app.repository.MemoryMessageStorage
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.communication.MessageManager
import de.robolab.client.communication.RobolabMessageProvider
import de.robolab.client.communication.mqtt.RobolabMqttConnection
import de.robolab.client.repl.commands.group.GroupCommand
import de.robolab.client.utils.MqttStorage
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.utils.cache.MemoryCacheStorage
import de.robolab.client.utils.cache.PersistentCacheStorage
import de.robolab.common.utils.ConsoleGreeter
import de.robolab.common.utils.Logger
import de.robolab.common.utils.autoLogger
import de.westermann.kobserve.property.flatMapBinding
import de.westermann.kobserve.property.mapBinding
import de.westermann.kobserve.property.nullableFlatMapBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

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
        autoLogger.debug("Could not create PersistentCacheStorage",e)
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
        fileImportController,
    )
    val toolBarController = ToolBarController(
        contentController,
        remoteServerController,
        uiController,
    )

    val infoBarController = InfoBarController(
        contentController,
        uiController,
    )
    val statusBarController = StatusBarController(
        connectionController,
        contentController,
        progressController,
        uiController
    )

    val macroController = MacroController()

    init {
        ConsoleGreeter.greetClient()

        messageManager.onMessage {
            messageRepository.addMessage(it)
        }
        messageManager.onMessageList {
            messageRepository.addMessageList(it)
        }

        GroupCommand.bind(this)
    }

    fun appendLiveGroupView(groups: List<String>, customIndex: Int? = null) {
        GlobalScope.launch {

            if(customIndex != null && groups.size == 1) {
                val groupName = groups[0]
                val group = messageRepository.createEmptyGroup(groupName) ?: return@launch
                val attempt = messageRepository.getLatestAttempt(group.groupId)

                val document = GroupLiveAttemptPlanetDocument(
                    group,
                    attempt,
                    messageRepository,
                    messageManager,
                    filePlanetController,
                    uiController
                )

                withContext(Dispatchers.Main) {
                    contentController.openDocumentAtIndex(document, customIndex, false)
                }
            } else {
                for ((index, groupName) in groups.withIndex()) {
                    if (groupName.isBlank()) continue
                    val group = messageRepository.createEmptyGroup(groupName) ?: continue
                    val attempt = messageRepository.getLatestAttempt(group.groupId)

                    val document = GroupLiveAttemptPlanetDocument(
                        group,
                        attempt,
                        messageRepository,
                        messageManager,
                        filePlanetController,
                        uiController
                    )

                    withContext(Dispatchers.Main) {
                        contentController.openDocumentAtIndex(document, index, false)
                    }
                }
            }
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
            appendLiveGroupView(groups)
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
        val dateTime: Instant,
        val content: suspend () -> Sequence<String>
    )
}
