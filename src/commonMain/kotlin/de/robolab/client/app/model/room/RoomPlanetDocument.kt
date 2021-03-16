package de.robolab.client.app.model.room

import de.robolab.client.app.controller.FilePlanetController
import de.robolab.client.app.controller.ui.InfoBarController
import de.robolab.client.app.controller.ui.UiController
import de.robolab.client.app.model.base.IInfoBarContent
import de.robolab.client.app.model.base.IPlanetDocument
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.model.base.ToolBarEntry
import de.robolab.client.app.repository.Attempt
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.app.repository.Room
import de.robolab.client.app.viewmodel.FormContentViewModel
import de.robolab.client.app.viewmodel.SideBarTabViewModel
import de.robolab.client.communication.RobolabMessage
import de.robolab.client.communication.toRobot
import de.robolab.client.renderer.drawable.live.RobotDrawable
import de.robolab.client.renderer.drawable.planet.MultiRobotPlanetDrawable
import de.robolab.client.utils.runAsync
import de.robolab.common.planet.Planet
import de.westermann.kobserve.base.ObservableProperty
import de.westermann.kobserve.base.ObservableValue
import de.westermann.kobserve.event.EventListener
import de.westermann.kobserve.event.now
import de.westermann.kobserve.list.observableListOf
import de.westermann.kobserve.property.constObservable
import de.westermann.kobserve.property.property
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RoomPlanetDocument(
    val room: Room,
    private val messageRepository: MessageRepository,
    planetProvider: FilePlanetController,
    private val uiController: UiController
) : IPlanetDocument {

    val messages = observableListOf<RobolabMessage>()

    override val nameProperty = constObservable("Room: ${room.name}")

    override val toolBarLeft = constObservable<List<FormContentViewModel>>(emptyList())
    override val toolBarRight = constObservable<List<FormContentViewModel>>(emptyList())


    val drawable = MultiRobotPlanetDrawable()
    override val documentProperty = constObservable(drawable.view)

    data class GroupState(
        val attempt: Attempt,
        val messages: List<RobolabMessage>,
        val robot: RobotDrawable.Robot
    ) {
        fun description() = buildString {
            if (robot.beforePoint) {
                append("Before point ")
            } else {
                append("After point ")
            }
            append(robot.point.x)
            append(", ")
            append(robot.point.y)
            append(" ")
            append(robot.direction.name)
        }
    }

    val groupStateList = property(listOf<GroupState>())

    private var attemptSet = emptySet<Attempt>()
    fun update(onlyUpdateAttempt: Attempt? = null) {
        if (!isAttached) return

        GlobalScope.launch(Dispatchers.Default) {
            if (onlyUpdateAttempt == null) {
                attemptSet = messageRepository.getRoomAttemptList(room.roomId).toSet()
            } else {
                val toRemove = attemptSet.find { it.attemptId == onlyUpdateAttempt.attemptId }
                if (toRemove != null) {
                    attemptSet = attemptSet - toRemove
                }
                attemptSet = attemptSet + onlyUpdateAttempt
            }

            val groupState = attemptSet.mapNotNull { attempt ->
                val messages = messageRepository.getAttemptMessageList(attempt.attemptId)
                GroupState(
                    attempt,
                    messages,
                    messages.toRobot(attempt.groupName.toIntOrNull()) ?: return@mapNotNull null
                )
            }

            runAsync {
                groupStateList.value = groupState
                drawable.importRobots(groupState.map { it.robot })
            }
        }
    }

    override val canUndoProperty = constObservable(false)

    override fun undo() {
    }

    override val canRedoProperty = constObservable(false)

    override fun redo() {
    }

    override fun onCreate() {
    }


    val infoBarTab = InfoBarRoomRobots(groupStateList, uiController)

    override val infoBarTabs: List<SideBarTabViewModel> = listOf(infoBarTab)
    override val activeTabProperty: ObservableProperty<SideBarTabViewModel?> = property(infoBarTab)

    private var isAttached = false

    private var ref1: EventListener<*>? = null
    private var ref2: EventListener<*>? = null
    override fun onAttach() {
        isAttached = true
        update()

        ref1?.detach()
        ref1 = messageRepository.onAttemptMessageListChange.reference {
            for (a in attemptSet) {
                if (a.attemptId == it.attemptId) {
                    update(it)
                }
            }
        }
        ref2?.detach()
        ref2 = messageRepository.onRoomAttemptListChange.reference {
            if (it.roomId == room.roomId) {
                update()
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
        if (other !is RoomPlanetDocument) return false

        if (room != other.room) return false

        return true
    }

    override fun hashCode(): Int {
        return room.hashCode()
    }

    init {
        val observable = planetProvider.getPlanetObservable(room.name)
        observable.onChange.now {
            val planet = observable.value
            drawable.importPlanet(planet)
        }
    }
}
