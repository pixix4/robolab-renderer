package de.robolab.client.app.repository

import de.robolab.client.communication.RobolabMessage

actual class DatabaseMessageStorage actual constructor() : IMessageStorage {
    
    override suspend fun <T> transaction(block: () -> T): T {
        throw UnsupportedOperationException()
    }

    override fun getGroup(groupId: GroupId): Group {
         throw UnsupportedOperationException()
    }

    override fun getGroupByName(groupName: String): Group? {
         throw UnsupportedOperationException()
    }

    override fun listAllGroups(): List<Group> {
         throw UnsupportedOperationException()
    }

    override fun createGroup(group: Group): Group {
         throw UnsupportedOperationException()
    }

    override fun updateGroup(group: Group): Group {
         throw UnsupportedOperationException()
    }

    override fun deleteGroup(groupId: GroupId) {
         throw UnsupportedOperationException()
    }

    override fun getAttempt(attemptId: AttemptId): Attempt {
         throw UnsupportedOperationException()
    }

    override fun listGroupAttempts(groupId: GroupId): List<Attempt> {
         throw UnsupportedOperationException()
    }

    override fun listRoomAttempts(roomId: RoomId): List<Attempt> {
         throw UnsupportedOperationException()
    }

    override fun createAttempt(attempt: Attempt): Attempt {
         throw UnsupportedOperationException()
    }

    override fun updateAttempt(attempt: Attempt): Attempt {
         throw UnsupportedOperationException()
    }

    override fun deleteAttempt(attemptId: AttemptId) {
         throw UnsupportedOperationException()
    }

    override fun getRoom(roomId: RoomId): Room {
         throw UnsupportedOperationException()
    }

    override fun getRoomByName(roomName: String): Room? {
         throw UnsupportedOperationException()
    }

    override fun listAllRooms(): List<Room> {
         throw UnsupportedOperationException()
    }

    override fun createRoom(room: Room): Room {
         throw UnsupportedOperationException()
    }

    override fun updateRoom(room: Room): Room {
         throw UnsupportedOperationException()
    }

    override fun deleteRoom(roomId: RoomId) {
         throw UnsupportedOperationException()
    }

    override fun getMessageList(attemptId: AttemptId): List<RobolabMessage> {
         throw UnsupportedOperationException()
    }

    override fun createMessage(message: RobolabMessage, attempt: Attempt) {
         throw UnsupportedOperationException()
    }

    override fun clear() {
        throw UnsupportedOperationException()
    }
}
