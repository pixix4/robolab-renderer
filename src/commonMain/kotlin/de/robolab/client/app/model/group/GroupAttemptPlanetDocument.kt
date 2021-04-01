package de.robolab.client.app.model.group

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import de.robolab.client.app.controller.FilePlanetController
import de.robolab.client.app.controller.ui.UiController
import de.robolab.client.app.repository.Attempt
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.app.viewmodel.FormContentViewModel
import de.robolab.client.app.viewmodel.SideBarTabViewModel
import de.robolab.client.communication.MessageManager
import de.robolab.client.renderer.utils.TransformationInteraction
import de.robolab.client.utils.runAsync
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.event.EventListener
import de.westermann.kobserve.event.now
import de.westermann.kobserve.list.sync
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GroupAttemptPlanetDocument(
    override val attempt: Attempt,
    private val messageRepository: MessageRepository,
    messageManager: MessageManager,
    private val planetProvider: FilePlanetController,
    private val uiController: UiController
) : AbstractGroupAttemptPlanetDocument() {

    override val nameProperty = constObservable(
        "Group ${attempt.groupName}: ${dateFormat.format(DateTimeTz.Companion.fromUnixLocal(attempt.startMessageTime))}"
    )

    override val toolBarLeft = constObservable<List<FormContentViewModel>>(emptyList())
    override val toolBarRight = constObservable<List<FormContentViewModel>>(emptyList())

    private val infoBarTab = InfoBarGroupMessages(
        constObservable(attempt),
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
        selectedIndexProperty.onChange.now { update() }
        messages.onChange.now {
            if (messages.lastIndex - 1 <= selectedIndexProperty.value) {
                selectedIndexProperty.value = messages.lastIndex
            }
        }

        planetNameProperty.onChange.now {
            val observable = planetProvider.getPlanetObservable(planetNameProperty.value)
            if (backgroundPlanet.isBound) {
                backgroundPlanet.unbind()
            }
            backgroundPlanet.bind(observable)
        }

        backgroundPlanet.onChange.now {
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
