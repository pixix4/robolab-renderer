package de.robolab.client.app.model.group

import com.soywiz.klock.DateFormat
import de.robolab.client.app.controller.InfoBarController
import de.robolab.client.app.model.base.IInfoBarContent
import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.base.ToolBarEntry
import de.robolab.client.app.model.file.CachedFilePlanetProvider
import de.robolab.client.app.repository.Attempt
import de.robolab.client.app.repository.Group
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.communication.*
import de.robolab.client.renderer.drawable.planet.LivePlanetDrawable
import de.robolab.client.renderer.utils.TransformationInteraction
import de.robolab.client.utils.runAsync
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventListener
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.list.sync
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class GroupLiveAttemptPlanetDocument(
    var group: Group,
    attempt: Attempt,
    private val messageRepository: MessageRepository,
    messageManager: MessageManager,
    private val planetProvider: CachedFilePlanetProvider
) : IPlanetDocument {

    private val latestAttemptProperty = property(attempt)
    private var latestAttempt by latestAttemptProperty

    val messages = observableListOf<RobolabMessage>()

    override val nameProperty = constObservable("Group ${group.name}")

    override val toolBarLeft = constObservable(emptyList<List<ToolBarEntry>>())
    override val toolBarRight = constObservable(emptyList<List<ToolBarEntry>>())

    val selectedIndexProperty = property(messages.lastIndex)

    override val canUndoProperty = property(selectedIndexProperty, messages) {
        selectedIndexProperty.value > 0
    }

    override fun undo() {
        selectedIndexProperty.value = max(0, selectedIndexProperty.value - 1)
    }

    override val canRedoProperty = property(selectedIndexProperty, messages) {
        val selectedIndex = selectedIndexProperty.value
        val lastIndex = messages.lastIndex

        selectedIndex < lastIndex
    }

    override fun redo() {
        selectedIndexProperty.value = min(messages.lastIndex, selectedIndexProperty.value + 1)
    }

    val drawable = LivePlanetDrawable()
    override val documentProperty = constObservable(drawable.view)


    val planetNameProperty = property("")
    private val backgroundPlanet = property<Planet>()

    val infoBarTab = object : InfoBarController.Tab {
        override val icon = MaterialIcon.INFO_OUTLINE
        override val tooltip = ""
        override fun open() {
        }
    }

    override val infoBarTabsProperty = constObservable(listOf(infoBarTab))

    override val infoBarActiveTabProperty = constObservable(infoBarTab)

    override val infoBarProperty: ObservableValue<IInfoBarContent> = constObservable(
        InfoBarGroupMessages(
            latestAttemptProperty,
            messages,
            selectedIndexProperty,
            planetNameProperty,
            messageManager,
            this::undo,
            this::redo
        )
    )

    private var serverPlanet = Planet.EMPTY
    private var mqttPlanet = Planet.EMPTY

    private var lastSelectedIndex = selectedIndexProperty.value
    fun update() {
        val selectedIndex = selectedIndexProperty.value
        val m = if (selectedIndex >= messages.lastIndex) messages else messages.take(selectedIndex + 1)

        val (sp, visitedPoints) = m.toServerPlanet()
        serverPlanet = sp
        if (planetNameProperty.value != serverPlanet.name) {
            planetNameProperty.value = serverPlanet.name
        }
        mqttPlanet = m.toMqttPlanet()

        if (!isAttached) return

        val planet = backgroundPlanet.value ?: Planet.EMPTY
        drawable.importServerPlanet(
            serverPlanet.importSplines(planet).importSenderGroups(planet, visitedPoints),
            true
        )
        drawable.importMqttPlanet(mqttPlanet.importSplines(planet))

        val backward = selectedIndex < lastSelectedIndex
        lastSelectedIndex = selectedIndex
        drawable.importRobot(m.toRobot(group.name.toIntOrNull(), backward))
    }

    private var isAttached = false

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
            if (it.attemptId == latestAttempt.attemptId) {
                updateMessageList()
            }
        }
        ref2 = messageRepository.onGroupAttemptListChange.reference {
            if (group.latestAttemptId != it.latestAttemptId) {
                group = it
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
            val observable = planetProvider[planetNameProperty.value]
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
            drawable.centerPlanet(TransformationInteraction.ANIMATION_TIME)
        }
    }

    companion object {
        private val dateFormat = DateFormat("HH:mm:ss")
    }
}
