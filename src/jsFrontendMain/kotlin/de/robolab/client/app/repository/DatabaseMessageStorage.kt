package de.robolab.client.app.repository

import de.robolab.client.communication.RobolabMessage

actual class DatabaseMessageStorage actual constructor() : IMessageStorage {

    private val storage = MemoryMessageStorage()

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
        return storage.createGroup(group)
    }

    override fun updateGroup(group: Group): Group {
        return storage.updateGroup(group)
    }

    override fun deleteGroup(groupId: GroupId) {
        return storage.deleteGroup(groupId)
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

    override fun createAttempt(attempt: Attempt): Attempt {
        return storage.createAttempt(attempt)
    }

    override fun updateAttempt(attempt: Attempt): Attempt {
        return storage.updateAttempt(attempt)
    }

    override fun deleteAttempt(attemptId: AttemptId) {
        return storage.deleteAttempt(attemptId)
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
        return storage.createRoom(room)
    }

    override fun updateRoom(room: Room): Room {
        return storage.updateRoom(room)
    }

    override fun deleteRoom(roomId: RoomId) {
        storage.deleteRoom(roomId)
    }

    override fun getMessageList(attemptId: AttemptId): List<RobolabMessage> {
        return storage.getMessageList(attemptId)
    }

    override fun createMessage(message: RobolabMessage, attempt: Attempt) {
        return storage.createMessage(message, attempt)
    }

    override fun clear() {
        storage.clear()
    }
}
