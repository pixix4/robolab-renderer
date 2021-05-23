package de.robolab.client.app.repository

import de.robolab.client.communication.RobolabMessage

interface IMessageStorage {

    suspend fun <T> transaction(block: () -> T): T

    fun getGroup(groupId: GroupId): Group
    fun getGroupByName(groupName: String): Group?
    fun listAllGroups(): List<Group>
    fun createGroup(group: Group): Group
    fun updateGroup(group: Group): Group
    fun deleteGroup(groupId: GroupId)

    fun getAttempt(attemptId: AttemptId): Attempt
    fun listGroupAttempts(groupId: GroupId): List<Attempt>
    fun listRoomAttempts(roomId: RoomId): List<Attempt>
    fun createAttempt(attempt: Attempt): Attempt
    fun updateAttempt(attempt: Attempt): Attempt
    fun deleteAttempt(attemptId: AttemptId)

    fun getRoom(roomId: RoomId): Room
    fun getRoomByName(roomName: String): Room?
    fun listAllRooms(): List<Room>
    fun createRoom(room: Room): Room
    fun updateRoom(room: Room): Room
    fun deleteRoom(roomId: RoomId)

    fun getMessageList(attemptId: AttemptId): List<RobolabMessage>
    fun createMessage(message: RobolabMessage, attempt: Attempt)

    fun clear()
}

value class GroupId(val id: Long) {
    companion object {
        val NEW = GroupId(-1L)
    }
}

value class AttemptId(val id: Long) {
    companion object {
        val NEW = AttemptId(-1L)
    }
}

value class RoomId(val id: Long) {
    companion object {
        val NEW = RoomId(-1L)
    }
}

data class Group(
    val groupId: GroupId,
    val name: String,
    val planet: String?,
    val attemptCount: Int,
    val latestAttemptId: AttemptId,
    val lastMessageTime: Long,
)


data class Attempt(
    val attemptId: AttemptId,
    val groupId: GroupId,
    val roomId: RoomId?,
    val groupName: String,
    val planet: String?,
    val messageCount: Int,
    val startMessageTime: Long,
    val lastMessageTime: Long,
)


data class Room(
    val roomId: RoomId,
    val name: String,
    val groupCount: Int,
    val lastMessageTime: Long,
)
