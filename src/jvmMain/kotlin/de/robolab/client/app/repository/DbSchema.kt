package de.robolab.client.app.repository

import de.robolab.client.communication.From
import org.jetbrains.exposed.sql.Table


object DbGroup : Table() {
    val id = long("id").autoIncrement()
    val name = varchar("name", 32).index(isUnique = true)
    val planet = varchar("planet", 128).nullable()
    val attemptCount = integer("attemptCount")
    val latestAttemptId = long("latestAttemptId")
    val lastMessageTime = long("lastMessageTime")

    override val primaryKey = PrimaryKey(id)
}

object DbAttempt : Table() {
    val id = long("id").autoIncrement()
    val groupId = long("groupId").index()
    val roomId = long("roomId").nullable().index()
    val groupName = varchar("groupName", 32)
    val planet = varchar("planet", 128).nullable()
    val messageCount = integer("attemptCount")
    val startMessageTime = long("startMessageTime")
    val lastMessageTime = long("lastMessageTime")

    override val primaryKey = PrimaryKey(id)
}

object DbRoom : Table() {
    val id = long("id").autoIncrement()
    val name = varchar("name", 128).index(isUnique = true)
    val groupCount = integer("groupCount")
    val lastMessageTime = long("lastMessageTime")

    override val primaryKey = PrimaryKey(id)
}

object DbMessage : Table() {
    val id = long("id").autoIncrement()
    val attemptId = long("attemptId").index()
    val time = long("time")
    val groupId = varchar("groupId", 32)
    val from = enumeration("from", From::class)
    val topic = varchar("topic", 128)
    val rawMessage = varchar("rawMessage", 1024)

    override val primaryKey = PrimaryKey(id)
}
