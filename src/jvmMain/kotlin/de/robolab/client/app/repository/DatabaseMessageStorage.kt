package de.robolab.client.app.repository

import de.robolab.client.communication.RobolabMessage
import de.robolab.client.communication.RobolabMessageProvider
import org.jetbrains.exposed.sql.*

actual class DatabaseMessageStorage : IMessageStorage {

    override suspend fun <T> transaction(block: () -> T): T {
        return dbQuery(block)
    }

    override fun getGroup(groupId: GroupId): Group {
        return DbGroup.select {
            DbGroup.id eq groupId.id
        }.first().toGroup()
    }

    override fun getGroupByName(groupName: String): Group? {
        return DbGroup.select {
            DbGroup.name eq groupName
        }.firstOrNull()?.toGroup()
    }

    override fun listAllGroups(): List<Group> {
        return DbGroup.selectAll()
            .orderBy(DbGroup.name)
            .map { it.toGroup() }
    }

    override fun createGroup(group: Group): Group {
        val id = DbGroup.insert {
            it[name] = group.name
            it[planet] = group.planet
            it[attemptCount] = group.attemptCount
            it[latestAttemptId] = group.latestAttemptId.id
            it[lastMessageTime] = group.lastMessageTime
        }[DbGroup.id]
        return group.copy(groupId = GroupId(id))
    }

    override fun updateGroup(group: Group): Group {
        DbGroup.update({ DbGroup.id eq group.groupId.id }) {
            it[name] = group.name
            it[planet] = group.planet
            it[attemptCount] = group.attemptCount
            it[latestAttemptId] = group.latestAttemptId.id
            it[lastMessageTime] = group.lastMessageTime
        }
        return group
    }

    override fun deleteGroup(groupId: GroupId) {
        DbGroup.deleteWhere { DbGroup.id eq groupId.id }
    }

    override fun getAttempt(attemptId: AttemptId): Attempt {
        return DbAttempt.select {
            DbAttempt.id eq attemptId.id
        }.first().toAttempt()
    }

    override fun listGroupAttempts(groupId: GroupId): List<Attempt> {
        return DbAttempt.select {
            DbAttempt.groupId eq groupId.id
        }.orderBy(DbAttempt.startMessageTime, SortOrder.DESC)
            .map { it.toAttempt() }
    }

    override fun listRoomAttempts(roomId: RoomId): List<Attempt> {
        return DbAttempt.select {
            DbAttempt.roomId eq roomId.id
        }.orderBy(DbAttempt.startMessageTime, SortOrder.DESC)
            .map { it.toAttempt() }
    }

    override fun createAttempt(attempt: Attempt): Attempt {
        val id = DbAttempt.insert {
            it[groupId] = attempt.groupId.id
            it[roomId] = attempt.roomId?.id
            it[groupName] = attempt.groupName
            it[planet] = attempt.planet
            it[messageCount] = attempt.messageCount
            it[startMessageTime] = attempt.startMessageTime
            it[lastMessageTime] = attempt.lastMessageTime
        }[DbAttempt.id]
        return attempt.copy(attemptId = AttemptId(id))
    }

    override fun updateAttempt(attempt: Attempt): Attempt {
        DbAttempt.update({DbAttempt.id eq attempt.attemptId.id}) {
            it[groupId] = attempt.groupId.id
            it[roomId] = attempt.roomId?.id
            it[groupName] = attempt.groupName
            it[planet] = attempt.planet
            it[messageCount] = attempt.messageCount
            it[startMessageTime] = attempt.startMessageTime
            it[lastMessageTime] = attempt.lastMessageTime
        }
        return attempt
    }

    override fun deleteAttempt(attemptId: AttemptId) {
        DbAttempt.deleteWhere { DbAttempt.id eq attemptId.id }
    }

    override fun getRoom(roomId: RoomId): Room {
        return DbRoom.select {
            DbRoom.id eq roomId.id
        }.first().toRoom()
    }

    override fun getRoomByName(roomName: String): Room? {
        return DbRoom.select {
            DbRoom.name eq roomName
        }.firstOrNull()?.toRoom()
    }

    override fun listAllRooms(): List<Room> {
        return DbRoom.selectAll()
            .orderBy(DbRoom.name)
            .map { it.toRoom() }
    }

    override fun createRoom(room: Room): Room {
        val id = DbRoom.insert {
            it[name] = room.name
            it[groupCount] = room.groupCount
            it[lastMessageTime] = room.lastMessageTime
        }[DbRoom.id]
        return room.copy(roomId = RoomId(id))
    }

    override fun updateRoom(room: Room): Room {
        DbRoom.update({ DbRoom.id eq room.roomId.id }) {
            it[name] = room.name
            it[groupCount] = room.groupCount
            it[lastMessageTime] = room.lastMessageTime
        }
        return room
    }

    override fun deleteRoom(roomId: RoomId) {
        DbRoom.deleteWhere { DbRoom.id eq roomId.id }
    }

    override fun getMessageList(attemptId: AttemptId): List<RobolabMessage> {
        return DbMessage.select {
            DbMessage.attemptId eq attemptId.id
        }.orderBy(DbMessage.time).mapNotNull { it.toMessage() }
    }

    override fun createMessage(message: RobolabMessage, attempt: Attempt) {
        DbMessage.insert {
            it[attemptId] = attempt.attemptId.id
            it[time] = message.metadata.time
            it[groupId] = message.metadata.groupId
            it[from] = message.metadata.from
            it[topic] = message.metadata.topic
            it[rawMessage] = message.metadata.rawMessage
        }
    }

    override fun clear() {
        DbConnection.reset()
    }

    init {
        DbConnection.init()
    }

    private fun ResultRow.toGroup() = Group(
        GroupId(this[DbGroup.id]),
        this[DbGroup.name],
        this[DbGroup.planet],
        this[DbGroup.attemptCount],
        AttemptId(this[DbGroup.latestAttemptId]),
        this[DbGroup.lastMessageTime],
    )

    private fun ResultRow.toAttempt() = Attempt(
        AttemptId(this[DbAttempt.id]),
        GroupId(this[DbAttempt.groupId]),
        this[DbAttempt.roomId]?.let(::RoomId),
        this[DbAttempt.groupName],
        this[DbAttempt.planet],
        this[DbAttempt.messageCount],
        this[DbAttempt.startMessageTime],
        this[DbAttempt.lastMessageTime],
    )

    private fun ResultRow.toRoom() = Room(
        RoomId(this[DbRoom.id]),
        this[DbRoom.name],
        this[DbRoom.groupCount],
        this[DbRoom.lastMessageTime],
    )

    private fun ResultRow.toMessage(): RobolabMessage? {
        val metadata = RobolabMessage.Metadata(
            this[DbMessage.time],
            this[DbMessage.groupId],
            this[DbMessage.from],
            this[DbMessage.topic],
            this[DbMessage.rawMessage],
        )

        return RobolabMessageProvider.parseMessage(metadata)
    }
}
