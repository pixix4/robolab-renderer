package de.robolab.client.app.repository

import de.robolab.client.communication.RobolabMessage

class MemoryMessageStorage : IMessageStorage {

    private val groupMap: MutableMap<GroupId, Group> = mutableMapOf()
    private var nexGroupId = 0L

    private val attemptMap: MutableMap<AttemptId, Attempt> = mutableMapOf()
    private var nexAttemptId = 0L

    private val roomMap: MutableMap<RoomId, Room> = mutableMapOf()
    private var nexRoomId = 0L

    private val messageMap: MutableMap<AttemptId, MutableList<RobolabMessage>> = mutableMapOf()

    override suspend fun <T> transaction(block: () -> T): T {
        return block()
    }

    override fun getGroup(groupId: GroupId): Group {
        return groupMap.getValue(groupId)
    }

    override fun getGroupByName(groupName: String): Group? {
        return groupMap.values.find { it.name == groupName }
    }

    override fun listAllGroups(): List<Group> {
        return groupMap.values.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name} )
    }

    override fun createGroup(group: Group): Group {
        val createdGroup = group.copy(
            groupId = GroupId(nexGroupId++)
        )
        groupMap[createdGroup.groupId] = createdGroup
        return createdGroup
    }

    override fun updateGroup(group: Group): Group {
        groupMap[group.groupId] = group
        return group
    }

    override fun deleteGroup(groupId: GroupId) {
        groupMap.remove(groupId)
    }

    override fun getAttempt(attemptId: AttemptId): Attempt {
        return attemptMap.getValue(attemptId)
    }

    override fun listGroupAttempts(groupId: GroupId): List<Attempt> {
        return attemptMap.values.filter {
            it.groupId == groupId
        }.sortedByDescending {
            it.startMessageTime
        }
    }

    override fun listRoomAttempts(roomId: RoomId): List<Attempt> {
        return attemptMap.values.filter {
            it.roomId == roomId
        }.sortedByDescending {
            it.startMessageTime
        }
    }

    override fun createAttempt(attempt: Attempt): Attempt {
        val createdAttempt = attempt.copy(
            attemptId = AttemptId(nexAttemptId++)
        )
        attemptMap[createdAttempt.attemptId] = createdAttempt
        return createdAttempt
    }

    override fun updateAttempt(attempt: Attempt): Attempt {
        attemptMap[attempt.attemptId] = attempt
        return attempt
    }

    override fun deleteAttempt(attemptId: AttemptId) {
        attemptMap.remove(attemptId)
    }

    override fun getRoom(roomId: RoomId): Room {
        return roomMap.getValue(roomId)
    }

    override fun getRoomByName(roomName: String): Room? {
        return roomMap.values.find { it.name == roomName }
    }

    override fun listAllRooms(): List<Room> {
        return roomMap.values.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name} )
    }

    override fun createRoom(room: Room): Room {
        val createdRoom = room.copy(
            roomId = RoomId(nexRoomId++)
        )
        roomMap[createdRoom.roomId] = createdRoom
        return createdRoom
    }

    override fun updateRoom(room: Room): Room {
        roomMap[room.roomId] = room
        return room
    }

    override fun deleteRoom(roomId: RoomId) {
        roomMap.remove(roomId)
    }

    override fun getMessageList(attemptId: AttemptId): List<RobolabMessage> {
        return messageMap[attemptId] ?: emptyList()
    }

    override fun createMessage(message: RobolabMessage, attempt: Attempt) {
        val list = messageMap.getOrPut(attempt.attemptId) {
            mutableListOf()
        }
        list += message
    }

    override fun clear() {
        groupMap.clear()
        nexGroupId = 0L

        attemptMap.clear()
        nexAttemptId = 0L

        roomMap.clear()
        nexRoomId = 0L

        messageMap.clear()
    }
}
