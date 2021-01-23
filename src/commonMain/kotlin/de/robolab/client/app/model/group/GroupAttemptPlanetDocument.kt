package de.robolab.client.app.model.group

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import de.robolab.client.app.controller.InfoBarController
import de.robolab.client.app.model.base.IInfoBarContent
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.base.ToolBarEntry
import de.robolab.client.app.model.file.CachedFilePlanetProvider
import de.robolab.client.app.repository.Attempt
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.communication.MessageManager
import de.robolab.client.renderer.utils.TransformationInteraction
import de.robolab.client.utils.runAsync
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventListener
import de.westermann.kobserve.list.sync
import de.westermann.kobserve.property.constObservable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GroupAttemptPlanetDocument(
    override val attempt: Attempt,
    private val messageRepository: MessageRepository,
    messageManager: MessageManager,
    private val planetProvider: CachedFilePlanetProvider
) : AbstractGroupAttemptPlanetDocument() {

    override val nameProperty = constObservable(
        "Group ${attempt.groupName}: ${dateFormat.format(DateTimeTz.Companion.fromUnixLocal(attempt.startMessageTime))}"
    )

    override val toolBarLeft = constObservable(emptyList<List<ToolBarEntry>>())
    override val toolBarRight = constObservable(emptyList<List<ToolBarEntry>>())

    private val infoBarTab = object : InfoBarController.Tab {
        override val icon = MaterialIcon.INFO_OUTLINE
        override val tooltip = ""
        override fun open() {
        }
    }

    override val infoBarTabsProperty: ObservableValue<List<InfoBarController.Tab>> = constObservable(listOf(infoBarTab))

    override val infoBarActiveTabProperty: ObservableValue<InfoBarController.Tab> = constObservable(infoBarTab)

    override val infoBarProperty: ObservableValue<IInfoBarContent> = constObservable(
        InfoBarGroupMessages(
            constObservable(attempt),
            messages,
            selectedIndexProperty,
            planetNameProperty,
            messageManager,
            this::undo,
            this::redo
        )
    )

    override var isAttached = false

    private fun updateMessageList() {
        GlobalScope.launch {
            val messageList = messageRepository
                .getAttemptMessageList(attempt.attemptId)

            runAsync {
                messages.sync(messageList)
            }
        }
    }

    private var ref: EventListener<*>? = null

    override fun onCreate() {
    }

    override fun onAttach() {
        isAttached = true
        update()

        ref?.detach()
        updateMessageList()
        ref = messageRepository.onAttemptMessageListChange.reference {
            if (it.attemptId == attempt.attemptId) {
                updateMessageList()
            }
        }
    }

    override fun onDetach() {
        isAttached = false
        ref?.detach()
    }

    override fun onDestroy() {
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GroupAttemptPlanetDocument) return false

        if (attempt != other.attempt) return false

        return true
    }

    override fun hashCode(): Int {
        return attempt.hashCode()
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
            drawable.centerPlanet(duration = TransformationInteraction.ANIMATION_TIME)
        }
    }

    companion object {
        private val dateFormat = DateFormat("HH:mm:ss")
    }
}
