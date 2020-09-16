package de.robolab.client.app.repository

import de.robolab.common.utils.Logger
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection.TRANSACTION_SERIALIZABLE
import kotlin.system.exitProcess

object DbConnection {

    private val logger = Logger("DbConnection")

    private val schemaList = arrayOf(
        DbGroup, DbRoom, DbAttempt, DbMessage
    )

    fun init() {
        try {
            // Database.connect("jdbc:h2:mem:regular;DB_CLOSE_DELAY=-1;", "org.h2.Driver")
            Database.connect("jdbc:h2:./cache.h2.db", "org.h2.Driver")
        } catch (e: ExceptionInInitializerError) {
            e.printStackTrace()
            exitProcess(1)
        }
        TransactionManager.manager.defaultIsolationLevel = TRANSACTION_SERIALIZABLE

        try {
            reset()
        } catch (e: ExposedSQLException) {
            logger.error { "Cannot initialize the database!" }

            print("Do you want to recreate the database (you will lose all data)? [y/N]: ")
            val result = readLine()

            if (result == null || result.toLowerCase() !in "yes") {
                exitProcess(1)
            } else {
                reset()
            }
        }
    }

    private fun create() {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(*schemaList)
        }
    }

    private fun delete() {
        transaction {
            SchemaUtils.drop(*schemaList)
        }
    }

    fun reset() {
        transaction {
            SchemaUtils.drop(*schemaList)
            SchemaUtils.create(*schemaList)
        }
    }
}

suspend fun <T> dbQuery(block: () -> T): T = newSuspendedTransaction(Dispatchers.IO) {
    synchronized(DbConnection) {
        block()
    }
}
