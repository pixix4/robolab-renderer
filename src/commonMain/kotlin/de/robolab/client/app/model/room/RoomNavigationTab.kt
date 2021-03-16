package de.robolab.client.app.model.room

import de.robolab.client.app.controller.FilePlanetController
import de.robolab.client.app.controller.ui.ContentController
import de.robolab.client.app.controller.ui.UiController
import de.robolab.client.app.model.base.INavigationBarEntry
import de.robolab.client.app.model.base.INavigationBarList
import de.robolab.client.app.model.base.INavigationBarTab
import de.robolab.client.app.model.base.MaterialIcon
import de.robolab.client.app.repository.Attempt
import de.robolab.client.app.repository.MessageRepository
import de.robolab.client.app.repository.Room
import de.robolab.client.app.viewmodel.FormContentViewModel
import de.robolab.client.app.viewmodel.SideBarContentViewModel
import de.robolab.client.app.viewmodel.buildFormContent
import de.robolab.client.communication.toRobot
import de.robolab.client.renderer.Exporter
import de.robolab.client.renderer.canvas.ICanvas
import de.robolab.client.renderer.drawable.planet.MultiRobotPlanetDrawable
import de.robolab.client.utils.PreferenceStorage
import de.robolab.common.utils.Dimension
import de.westermann.kobserve.property.property

class RoomNavigationTab(
    private val messageRepository: MessageRepository,
    private val contentController: ContentController,
    private val planetProvider: FilePlanetController,
    private val uiController: UiController
) : INavigationBarTab("Group tracked robots by planet name", MaterialIcon.PUBLIC) {


    override val contentProperty = property<SideBarContentViewModel>(
        RoomNavigationList(messageRepository, this)
    )

    private val searchProperty = property("")

    override val topToolBar = buildFormContent { }
    override val bottomToolBar = buildFormContent {
        input(searchProperty, typeHint = FormContentViewModel.InputTypeHint.SEARCH)
    }

    override fun openEntry(entry: INavigationBarEntry, asNewTab: Boolean) {
        if (entry is RoomNavigationList.Entry) {
            contentController.openDocument(
                RoomPlanetDocument(
                    entry.room,
                    messageRepository,
                    planetProvider,
                    uiController
                ), asNewTab
            )
        }
    }

    suspend fun <T : ICanvas> renderRoomPreview(room: Room, canvasCreator: (Dimension) -> T?): T? {
        val attemptSet = messageRepository.getRoomAttemptList(room.roomId).toSet()

        val drawable = MultiRobotPlanetDrawable()

        val groupState = attemptSet.mapNotNull { attempt ->
            val messages = messageRepository.getAttemptMessageList(attempt.attemptId)
            RoomPlanetDocument.GroupState(
                attempt,
                messages,
                messages.toRobot(attempt.groupName.toIntOrNull()) ?: return@mapNotNull null
            )
        }

        val backgroundPlanet = planetProvider.getPlanet(room.name)
        drawable.importPlanet(backgroundPlanet)
        drawable.importRobots(groupState.map { it.robot })

        val dimension = Exporter.getDimension(drawable)

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

    init {

        messageRepository.onRoomListChange {
            val active = contentProperty.value as? RepositoryEventListener
            active?.onRoomListChange()
        }
        messageRepository.onRoomAttemptListChange { id ->
            val active = contentProperty.value as? RepositoryEventListener
            active?.onRoomAttemptListChange(id)
        }

        messageRepository.onAttemptMessageListChange { id ->
            val active = contentProperty.value as? RepositoryEventListener
            active?.onAttemptMessageListChange(id)
        }
    }

    interface RepositoryEventListener {

        fun onRoomListChange() {}

        fun onRoomAttemptListChange(room: Room) {}

        fun onAttemptMessageListChange(attempt: Attempt) {}
    }

    interface RepositoryList : INavigationBarList, RepositoryEventListener
}
