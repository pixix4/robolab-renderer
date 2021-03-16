package de.robolab.client.app.model.group

import de.robolab.client.app.controller.FilePlanetController
import de.robolab.client.app.controller.ui.InfoBarController
import de.robolab.client.app.controller.ui.UiController
import de.robolab.client.app.model.base.IInfoBarContent
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.base.ToolBarEntry
import de.robolab.client.app.repository.Attempt
import de.robolab.client.app.repository.Group
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.app.viewmodel.FormContentViewModel
import de.robolab.client.app.viewmodel.SideBarTabViewModel
import de.robolab.client.communication.MessageManager
import de.robolab.client.renderer.utils.TransformationInteraction
import de.robolab.client.utils.runAsync
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventListener
import de.westermann.kobserve.list.sync
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GroupLiveAttemptPlanetDocument(
    var group: Group,
    attempt: Attempt,
    private val messageRepository: MessageRepository,
    messageManager: MessageManager,
    private val planetProvider: FilePlanetController,
    private val uiController: UiController
) : AbstractGroupAttemptPlanetDocument() {

    private val latestAttemptProperty = property(attempt)
    private var latestAttempt by latestAttemptProperty
    override val attempt by latestAttemptProperty

    override val nameProperty = constObservable("Group ${group.name}")

    override val toolBarLeft = constObservable<List<FormContentViewModel>>(emptyList())
    override val toolBarRight = constObservable<List<FormContentViewModel>>(emptyList())

    private val infoBarTab = InfoBarGroupMessages(
        latestAttemptProperty,
        messages,
        selectedIndexProperty,
        planetNameProperty,
        messageManager,
        this::undo,
        this::redo,
        uiController
    )

    override val infoBarTabs: List<SideBarTabViewModel> = listOf(infoBarTab)
    override val activeTabProperty: ObservableProperty<SideBarTabViewModel?> = property(infoBarTab)

    override var isAttached = false

    private fun updateMessageList() {
        GlobalScope.launch {
            val messageList = messageRepository
                .getAttemptMessageList(latestAttempt.attemptId)

            runAsync {
                messages.sync(messageList)
            }
        }
    }

    private fun updateAttempt() {
        GlobalScope.launch {
            group = messageRepository.getGroup(group.groupId)
            val attempt = messageRepository.getAttempt(group.latestAttemptId)

            val messageList = messageRepository
                .getAttemptMessageList(attempt.attemptId)

            runAsync {
                if (attempt != latestAttempt) {
                    latestAttempt = attempt
                    planetNameProperty.value = ""
                    messages.clear()
                }
                messages.sync(messageList)
            }
        }
    }

    private var ref1: EventListener<*>? = null
    private var ref2: EventListener<*>? = null

    override fun onCreate() {
    }

    override fun onAttach() {
        isAttached = true
        update()

        ref1?.detach()
        ref2?.detach()
        updateAttempt()
        ref1 = messageRepository.onAttemptMessageListChange.reference {
            if (it.groupId == group.groupId && it.attemptId == latestAttempt.attemptId) {
                updateMessageList()
            }
        }
        ref2 = messageRepository.onGroupAttemptListChange.reference {
            if (it.groupId == group.groupId && group.latestAttemptId != it.latestAttemptId) {
                updateAttempt()
            }
        }
    }

    override fun onDetach() {
        isAttached = false
        ref1?.detach()
        ref2?.detach()
    }

    override fun onDestroy() {
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GroupLiveAttemptPlanetDocument) return false

        if (group.groupId != other.group.groupId) return false

        return true
    }

    override fun hashCode(): Int {
        return group.groupId.hashCode()
    }

    init {
        selectedIndexProperty.onChange { update() }
        messages.onChange {
            if (messages.lastIndex - 1 <= selectedIndexProperty.value) {
                selectedIndexProperty.value = messages.lastIndex
            }
        }

        planetNameProperty.onChange {
            val observable = planetProvider.getPlanetObservable(planetNameProperty.value)
            if (backgroundPlanet.isBound) {
                backgroundPlanet.unbind()
            }
            backgroundPlanet.bind(observable)
        }

        backgroundPlanet.onChange {
            val planet = backgroundPlanet.value ?: Planet.EMPTY
            drawable.importBackgroundPlanet(planet, true)
            drawable.importServerPlanet(serverPlanet.importSplines(planet), true)
            drawable.importMqttPlanet(mqttPlanet.importSplines(planet))
            drawable.autoCentering = true
            drawable.centerPlanet(duration = TransformationInteraction.ANIMATION_TIME)
        }
    }
}
