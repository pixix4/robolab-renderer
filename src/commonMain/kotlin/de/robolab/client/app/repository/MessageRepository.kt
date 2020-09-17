package de.robolab.client.app.repository

import com.soywiz.klock.DateTime
import de.robolab.client.communication.RobolabMessage
import de.robolab.client.utils.runAfterTimeoutInterval
import de.robolab.common.utils.Logger
import de.westermann.kobserve.event.EventHandler
import de.westermann.kobserve.event.subscribe
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MessageRepository(
    private val storage: IMessageStorage
) {
    private val logger =Logger(this)

    val onGroupListChange = EventHandler<Unit>()
    val onGroupAttemptListChange = EventHandler<Group>()
    val onRoomListChange = EventHandler<Unit>()
    val onRoomAttemptListChange = EventHandler<Room>()
    val onAttemptMessageListChange = EventHandler<Attempt>()

    private fun processMessage(message: RobolabMessage, storage: EventMessageStorage) {
        try {
            val groupName = message.metadata.groupId
            val planetName = if (message is RobolabMessage.PlanetMessage) message.planetName else null

            var group = storage.getGroupByName(groupName) ?: storage.createGroup(
                Group(
                    groupId = GroupId.NEW,
                    name = groupName,
                    planet = planetName,
                    attemptCount = 0,
                    latestAttemptId = AttemptId.NEW,
                    lastMessageTime = message.metadata.time
                )
            )

            var attempt = if (group.attemptCount == 0 || message is RobolabMessage.ReadyMessage) {
                val lastAttempt = storage.listGroupAttempts(group.groupId).firstOrNull()

                if (lastAttempt?.roomId != null) {
                    storage.updateAttempt(
                        lastAttempt.copy(
                            roomId = null
                        )
                    )

                    storage.updateRoom(storage.getRoom(lastAttempt.roomId).let {
                        it.copy(groupCount = it.groupCount - 1)
                    })
                }

                val attempt = storage.createAttempt(
                    Attempt(
                        attemptId = AttemptId.NEW,
                        groupId = group.groupId,
                        roomId = null,
                        groupName = groupName,
                        planet = planetName,
                        messageCount = 0,
                        startMessageTime = message.metadata.time,
                        lastMessageTime = message.metadata.time,
                    ), group
                )

                group = storage.updateGroup(
                    group.copy(
                        attemptCount = group.attemptCount + 1,
                        latestAttemptId = attempt.attemptId
                    )
                )

                attempt
            } else {
                storage.listGroupAttempts(group.groupId).first()
            }

            val attemptRoomId = attempt.roomId
            when {
                attemptRoomId != null -> {
                    storage.updateRoom(
                        storage.getRoom(attemptRoomId).copy(
                            lastMessageTime = message.metadata.time
                        )
                    )
                }
                planetName != null -> {
                    val room = storage.getRoomByName(planetName) ?: storage.createRoom(
                        Room(
                            roomId = RoomId.NEW,
                            name = planetName,
                            groupCount = 0,
                            lastMessageTime = message.metadata.time
                        )
                    )

                    attempt = storage.updateAttempt(
                        attempt.copy(
                            roomId = room.roomId,
                            planet = planetName
                        )
                    )
                    group = storage.updateGroup(
                        group.copy(
                            planet = planetName
                        )
                    )

                    storage.updateRoom(
                        room.copy(
                            lastMessageTime = message.metadata.time,
                            groupCount = room.groupCount + 1
                        )
                    )
                }
            }

            storage.updateAttempt(
                attempt.copy(
                    messageCount = attempt.messageCount + 1,
                    lastMessageTime = message.metadata.time
                )
            )

            storage.updateGroup(
                group.copy(
                    lastMessageTime = message.metadata.time
                )
            )

            storage.createMessage(message, attempt)
        } catch (e: Exception) {
            logger.error("Error processing mqtt message: ${e.message}\n${e.stackTraceToString()}")
        }
    }

    private fun processCleanupRoomAttemptList(threshold: Long, storage: EventMessageStorage) {
        val roomList = storage.listAllRooms()

        val thresholdTime = DateTime.nowUnixLong() - threshold
        for (room in roomList) {
            val outdatedAttempts = storage.listRoomAttempts(room.roomId).filter {
                it.lastMessageTime < thresholdTime
            }

            if (outdatedAttempts.isNotEmpty()) {
                val update = room.copy(
                    groupCount = room.groupCount - outdatedAttempts.size
                )
                storage.updateRoom(update)

                for (attempt in outdatedAttempts) {
                    storage.updateAttempt(
                        attempt.copy(
                            roomId = null
                        )
                    )
                }
            }
        }
    }

    suspend fun addMessage(message: RobolabMessage) {
        val events = EventMessageStorage(
            storage,
            onGroupListChange,
            onGroupAttemptListChange,
            onRoomListChange,
            onRoomAttemptListChange,
            onAttemptMessageListChange
        )

        storage.transaction {
            processMessage(message, events)
        }

        events.emit()
    }

    suspend fun addMessageList(messageList: List<RobolabMessage>) {
        val events = EventMessageStorage(
            storage,
            onGroupListChange,
            onGroupAttemptListChange,
            onRoomListChange,
            onRoomAttemptListChange,
            onAttemptMessageListChange
        )
        storage.transaction {
            for (message in messageList) {
                processMessage(message, events)
            }
            processCleanupRoomAttemptList(CLEANUP_THRESHOLD, events)
        }

        events.emit()
    }

    suspend fun clear() {
        val events = EventMessageStorage(
            storage,
            onGroupListChange,
            onGroupAttemptListChange,
            onRoomListChange,
            onRoomAttemptListChange,
            onAttemptMessageListChange
        )

        storage.transaction {
            events.clear()
        }

        events.emit()
    }

    suspend fun cleanupRoomAttemptList(threshold: Long) {
        val events = EventMessageStorage(
            storage,
            onGroupListChange,
            onGroupAttemptListChange,
            onRoomListChange,
            onRoomAttemptListChange,
            onAttemptMessageListChange
        )

        storage.transaction {
            processCleanupRoomAttemptList(threshold, events)
        }

        events.emit()
    }

    suspend fun getGroupList(): List<Group> = storage.transaction {
        storage.listAllGroups()
    }

    suspend fun getGroup(groupId: GroupId): Group = storage.transaction {
        storage.getGroup(groupId)
    }

    suspend fun getGroupAttemptList(groupId: GroupId): List<Attempt> = storage.transaction {
        storage.listGroupAttempts(groupId)
    }

    suspend fun getRoomList(): List<Room> = storage.transaction {
        storage.listAllRooms()
    }

    suspend fun getRoom(roomId: RoomId): Room = storage.transaction {
        storage.getRoom(roomId)
    }

    suspend fun getRoomAttemptList(roomId: RoomId): List<Attempt> = storage.transaction {
        storage.listRoomAttempts(roomId)
    }

    suspend fun getAttemptMessageList(attemptId: AttemptId): List<RobolabMessage> = storage.transaction {
        storage.getMessageList(attemptId)
    }

    suspend fun getAttempt(attemptId: AttemptId): Attempt = storage.transaction {
        storage.getAttempt(attemptId)
    }

    suspend fun getLatestAttempt(groupId: GroupId): Attempt = storage.transaction {
        val group = storage.getGroup(groupId)
        storage.getAttempt(group.latestAttemptId)
    }

    class EventMessageStorage(
        private val storage: IMessageStorage,
        private val onGroupListChange: EventHandler<Unit>,
        private val onGroupAttemptListChange: EventHandler<Group>,
        private val onRoomListChange: EventHandler<Unit>,
        private val onRoomAttemptListChange: EventHandler<Room>,
        private val onAttemptMessageListChange: EventHandler<Attempt>,
    ) : IMessageStorage {

        private val emitGroupListChange = mutableListOf<Unit>()
        private val emitGroupAttemptListChange = mutableListOf<Group>()
        private val emitRoomListChange = mutableListOf<Unit>()
        private val emitRoomAttemptListChange = mutableListOf<Room>()
        private val emitAttemptMessageListChange = mutableListOf<Attempt>()

        fun emit() {
            for (event in emitGroupListChange.distinct()) {
                // println("emitGroupListChange: $event")
                onGroupListChange.emit(event)
            }
            emitGroupListChange.clear()
            for (event in emitGroupAttemptListChange.asReversed().distinctBy { it.groupId }.asReversed()) {
                // println("emitGroupAttemptListChange: $event")
                onGroupAttemptListChange.emit(event)
            }
            emitGroupAttemptListChange.clear()
            for (event in emitRoomListChange.distinct()) {
                // println("emitRoomListChange: $event")
                onRoomListChange.emit(event)
            }
            emitRoomListChange.clear()
            for (event in emitRoomAttemptListChange.asReversed().distinctBy { it.roomId }.asReversed()) {
                // println("emitRoomAttemptListChange: $event")
                onRoomAttemptListChange.emit(event)
            }
            emitRoomAttemptListChange.clear()
            for (event in emitAttemptMessageListChange.asReversed().distinctBy { it.attemptId }.asReversed()) {
                // println("emitAttemptMessageListChange: $event")
                onAttemptMessageListChange.emit(event)
            }
            emitAttemptMessageListChange.clear()
        }

        override suspend fun <T> transaction(block: () -> T): T {
            return storage.transaction(block)
        }

        override fun getGroup(groupId: GroupId): Group {
            return storage.getGroup(groupId)
        }

        override fun getGroupByName(groupName: String): Group? {
            return storage.getGroupByName(groupName)
        }

        override fun listAllGroups(): List<Group> {
            return storage.listAllGroups()
        }

        override fun createGroup(group: Group): Group {
            val createdGroup = storage.createGroup(group)
            emitGroupListChange += Unit
            return createdGroup
        }

        override fun updateGroup(group: Group): Group {
            val updatedGroup = storage.updateGroup(group)
            emitGroupAttemptListChange += updatedGroup
            return updatedGroup
        }

        override fun deleteGroup(groupId: GroupId) {
            storage.deleteGroup(groupId)
            emitGroupListChange += Unit
        }

        override fun getAttempt(attemptId: AttemptId): Attempt {
            return storage.getAttempt(attemptId)
        }

        override fun listGroupAttempts(groupId: GroupId): List<Attempt> {
            return storage.listGroupAttempts(groupId)
        }

        override fun listRoomAttempts(roomId: RoomId): List<Attempt> {
            return storage.listRoomAttempts(roomId)
        }

        fun createAttempt(attempt: Attempt, group: Group): Attempt {
            val createdAttempt = storage.createAttempt(attempt)
            emitGroupAttemptListChange += group
            return createdAttempt
        }

        override fun createAttempt(attempt: Attempt): Attempt {
            return storage.createAttempt(attempt)
        }

        override fun updateAttempt(attempt: Attempt): Attempt {
            val updatedAttempt = storage.updateAttempt(attempt)
            emitAttemptMessageListChange += attempt
            return updatedAttempt
        }

        fun deleteAttempt(attemptId: AttemptId, group: Group) {
            storage.deleteAttempt(attemptId)
            emitGroupAttemptListChange += group
        }

        override fun deleteAttempt(attemptId: AttemptId) {
            storage.deleteAttempt(attemptId)
        }

        override fun getRoom(roomId: RoomId): Room {
            return storage.getRoom(roomId)
        }

        override fun getRoomByName(roomName: String): Room? {
            return storage.getRoomByName(roomName)
        }

        override fun listAllRooms(): List<Room> {
            return storage.listAllRooms()
        }

        override fun createRoom(room: Room): Room {
            val createdRoom = storage.createRoom(room)
            emitRoomListChange += Unit
            return createdRoom
        }

        override fun updateRoom(room: Room): Room {
            val updatedRoom = storage.updateRoom(room)
            emitRoomAttemptListChange += updatedRoom
            return updatedRoom
        }

        override fun deleteRoom(roomId: RoomId) {
            storage.deleteRoom(roomId)
            emitRoomListChange += Unit
        }

        override fun getMessageList(attemptId: AttemptId): List<RobolabMessage> {
            return storage.getMessageList(attemptId)
        }

        override fun createMessage(message: RobolabMessage, attempt: Attempt) {
            storage.createMessage(message, attempt)
            emitAttemptMessageListChange += attempt
        }

        override fun clear() {
            storage.clear()
            emitGroupListChange += Unit
            emitRoomListChange += Unit
        }
    }

    init {
        subscribe<ClearStorageEvent> {
            GlobalScope.launch {
                clear()
            }
        }

        runAfterTimeoutInterval(CLEANUP_INTERVAL) {
            GlobalScope.launch {
                cleanupRoomAttemptList(CLEANUP_THRESHOLD)
            }
        }
    }

    companion object {
        const val CLEANUP_THRESHOLD = 10L * 60L * 1000L
        const val CLEANUP_INTERVAL = 30L * 1000L
    }

    object ClearStorageEvent
}
