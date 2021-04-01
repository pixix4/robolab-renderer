package de.robolab.client.app.model.group

import de.robolab.client.app.controller.FilePlanetController
import de.robolab.client.app.controller.ui.ContentController
import de.robolab.client.app.controller.ui.UiController
import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.INavigationBarList
import de.robolab.client.app.model.base.INavigationBarTab
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.repository.Attempt
import de.robolab.client.app.repository.Group
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.app.viewmodel.FormContentViewModel
import de.robolab.client.app.viewmodel.SideBarContentViewModel
import de.robolab.client.app.viewmodel.buildFormContent
import de.robolab.client.communication.MessageManager
import de.robolab.client.communication.toMqttPlanet
import de.robolab.client.communication.toRobot
import de.robolab.client.communication.toServerPlanet
import de.robolab.client.renderer.Exporter
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.drawable.planet.LivePlanetDrawable
import de.robolab.client.utils.PreferenceStorage
import de.robolab.client.utils.runAsync
import de.robolab.common.utils.Dimension
import de.westermann.kobserve.property.property
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GroupNavigationTab(
    private val messageRepository: MessageRepository,
    private val messageManager: MessageManager,
    private val contentController: ContentController,
    private val planetProvider: FilePlanetController,
    private val uiController: UiController
) : INavigationBarTab("Track robots via mqtt", MaterialIcon.GROUP) {

    override val contentProperty = property<SideBarContentViewModel>(
        GroupNavigationList(messageRepository, this)
    )

    private val searchProperty = property("")

    override val topToolBar = buildFormContent { }
    override val bottomToolBar = buildFormContent {
        input(searchProperty, typeHint = FormContentViewModel.InputTypeHint.SEARCH) {
            onSubmit { searchValue ->
                val value = searchValue.trim()
                if (children.isEmpty() && value.isNotEmpty()) {
                    searchProperty.value = ""
                    GlobalScope.launch {
                        messageRepository.createEmptyGroup(value)
                    }
                }
            }
        }
        button(MaterialIcon.ADD, description = "Open log file") {
            // TODO
        }
    }

    private fun openGroupAttempt(attempt: Attempt, asNewTab: Boolean) {
        contentController.openDocument(
            GroupAttemptPlanetDocument(
                attempt,
                messageRepository,
                messageManager,
                planetProvider,
                uiController
            ), asNewTab
        )
    }

    private fun openGroupLiveAttempt(group: Group, asNewTab: Boolean) {
        GlobalScope.launch {
            val attempt = messageRepository.getLatestAttempt(group.groupId)
            runAsync {
                contentController.openDocument(
                    GroupLiveAttemptPlanetDocument(
                        group,
                        attempt,
                        messageRepository,
                        messageManager,
                        planetProvider,
                        uiController
                    ), asNewTab
                )
            }
        }
    }

    suspend fun <T : ICanvas> renderGroupLiveAttemptPreview(group: Group, canvasCreator: (Dimension) -> T?): T? {
        val attempt = messageRepository.getLatestAttempt(group.groupId)
        return renderGroupAttemptPreview(attempt, canvasCreator)
    }

    suspend fun <T : ICanvas> renderGroupAttemptPreview(attempt: Attempt, canvasCreator: (Dimension) -> T?): T? {
        val m = messageRepository.getAttemptMessageList(attempt.attemptId)

        val drawable = LivePlanetDrawable()

        val (serverPlanet, visitedPoints) = m.toServerPlanet()
        val backgroundPlanet = planetProvider.getPlanet(serverPlanet.name)
        val mqttPlanet = m.toMqttPlanet()

        drawable.importBackgroundPlanet(backgroundPlanet, true)
        drawable.importServerPlanet(
            serverPlanet.importSplines(backgroundPlanet).importSenderGroups(backgroundPlanet, visitedPoints),
            true
        )
        drawable.importMqttPlanet(mqttPlanet.importSplines(backgroundPlanet))

        drawable.importRobot(m.toRobot(attempt.groupName.toIntOrNull()))

        val dimension = Exporter.getDimension(drawable)
        if (dimension < Dimension.ONE) return null

        val canvas = canvasCreator(dimension)
        if (canvas != null) {
            Exporter.renderToCanvas(
                drawable,
                canvas,
                drawName = false,
                drawNumbers = false,
                theme = PreferenceStorage.selectedTheme.theme
            )
        }
        return canvas
    }

    override fun openEntry(entry: INavigationBarEntry, asNewTab: Boolean) {
        when (entry) {
            is GroupNavigationList.Entry -> {
                if (asNewTab) {
                    openGroupLiveAttempt(entry.group, false)
                } else {
                    searchProperty.value = ""
                    contentProperty.value =
                        GroupAttemptNavigationList(
                            entry.group,
                            messageRepository,
                            this,
                            contentProperty.value as? INavigationBarList ?: return
                        )
                }
            }
            is GroupAttemptNavigationList.Entry -> {
                openGroupAttempt(entry.attempt, asNewTab)
            }
            is GroupAttemptNavigationList.LiveEntry -> {
                openGroupLiveAttempt(entry.group, asNewTab)
            }
        }

    }

    init {
        messageRepository.onGroupListChange {
            val active = contentProperty.value as? RepositoryEventListener
            active?.onGroupListChange()
        }
        messageRepository.onGroupAttemptListChange { id ->
            val active = contentProperty.value as? RepositoryEventListener
            active?.onGroupAttemptListChange(id)
        }
        messageRepository.onAttemptMessageListChange { id ->
            val active = contentProperty.value as? RepositoryEventListener
            active?.onAttemptMessageListChange(id)
        }
    }

    interface RepositoryEventListener {

        fun onGroupListChange() {}

        fun onGroupAttemptListChange(group: Group) {}

        fun onAttemptMessageListChange(attempt: Attempt) {}
    }

    interface RepositoryList : INavigationBarList, RepositoryEventListener
}
