package de.robolab.client.app.model.group

import de.robolab.client.app.controller.TabController
import de.robolab.client.app.model.base.*
import de.robolab.client.app.model.file.CachedFilePlanetProvider
import de.robolab.client.app.repository.Attempt
import de.robolab.client.app.repository.Group
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.communication.MessageManager
import de.robolab.client.communication.toMqttPlanet
import de.robolab.client.communication.toRobot
import de.robolab.client.communication.toServerPlanet
import de.robolab.client.renderer.Exporter
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.drawable.planet.LivePlanetDrawable
import de.robolab.client.utils.runAsync
import de.robolab.common.planet.Planet
import de.robolab.common.utils.Dimension
import de.westermann.kobserve.property.constObservable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupNavigationTab(
    private val messageRepository: MessageRepository,
    private val messageManager: MessageManager,
    private val tabController: TabController,
    val planetProvider: CachedFilePlanetProvider
) : INavigationBarTab("Track robots via mqtt", MaterialIcon.GROUP, emptyList()) {

    override fun selectMode(mode: String) {}
    override val modeProperty = constObservable("")

    override fun submitSearch() {
        val value = searchProperty.value.trim()
        if (childrenProperty.value.isEmpty() && value.isNotEmpty()) {
            GlobalScope.launch {
                messageRepository.createEmptyGroup(value)
                withContext(Dispatchers.Main) {
                    searchProperty.value = ""
                }
            }
        }
    }

    private fun openGroupAttempt(attempt: Attempt, asNewTab: Boolean) {
        tabController.open(
            GroupAttemptPlanetDocument(
                attempt,
                messageRepository,
                messageManager,
                planetProvider
            ), asNewTab
        )
    }

    private fun openGroupLiveAttempt(group: Group, asNewTab: Boolean) {
        GlobalScope.launch {
            val attempt = messageRepository.getLatestAttempt(group.groupId)
            runAsync {
                tabController.open(
                    GroupLiveAttemptPlanetDocument(
                        group,
                        attempt,
                        messageRepository,
                        messageManager,
                        planetProvider
                    ), asNewTab
                )
            }
        }
    }

    suspend fun<T: ICanvas> renderGroupLiveAttemptPreview(group: Group, canvasCreator: (Dimension) -> T?): T? {
        val attempt = messageRepository.getLatestAttempt(group.groupId)
        return renderGroupAttemptPreview(attempt, canvasCreator)
    }

    suspend fun<T: ICanvas> renderGroupAttemptPreview(attempt: Attempt, canvasCreator: (Dimension) -> T?): T? {
        val m = messageRepository.getAttemptMessageList(attempt.attemptId)

        val drawable = LivePlanetDrawable()

        val (serverPlanet, visitedPoints) = m.toServerPlanet()
        val backgroundPlanet = planetProvider.loadPlanet(serverPlanet.name)
        val mqttPlanet = m.toMqttPlanet()

        val planet = backgroundPlanet ?: Planet.EMPTY
        drawable.importBackgroundPlanet(planet, true)
        drawable.importServerPlanet(
            serverPlanet.importSplines(planet).importSenderGroups(planet, visitedPoints),
            true
        )
        drawable.importMqttPlanet(mqttPlanet.importSplines(planet))

        drawable.importRobot(m.toRobot(attempt.groupName.toIntOrNull()))

        val dimension = Exporter.getDimension(drawable)

        val canvas = canvasCreator(dimension)
        if (canvas != null) {
            Exporter.renderToCanvas(drawable, canvas, drawName = false, drawNumbers = false)
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
                    activeProperty.value = GroupAttemptNavigationList(entry.group, messageRepository, this, activeProperty.value)
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
        activeProperty.value = GroupNavigationList(messageRepository, this)

        messageRepository.onGroupListChange {
            val active = activeProperty.value as? RepositoryEventListener
            active?.onGroupListChange()
        }
        messageRepository.onGroupAttemptListChange { id ->
            val active = activeProperty.value as? RepositoryEventListener
            active?.onGroupAttemptListChange(id)
        }

        messageRepository.onAttemptMessageListChange { id ->
            val active = activeProperty.value as? RepositoryEventListener
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
