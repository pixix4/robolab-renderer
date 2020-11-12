package de.robolab.server.data

import de.robolab.client.net.ICredentialProvider
import de.robolab.server.config.Config
import de.robolab.server.externaljs.JSArray
import de.robolab.server.externaljs.pg.Client
import de.robolab.server.externaljs.pg.obtainClient
import de.robolab.server.externaljs.pg.withConnection
import de.robolab.server.externaljs.toList
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

object PostgresMQTTCredentialStore {
    val client: Client = obtainClient(Config.MQTT.database)


    private val connectionRLock: ReentrantLock = ReentrantLock()

    private var storedUserInfos: Map<String, MQTTUserInfo>? = null


    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    suspend fun getMQTTUserInfos(): Map<String, MQTTUserInfo> {
        var storedInfo = storedUserInfos
        if (storedInfo != null) return storedInfo
        connectionRLock.withLock {
            storedInfo = storedUserInfos
            if (storedInfo != null) return storedInfo!!
            client.withConnection {
                try {
                    val result =
                        client.query(
                            "SELECT v.username, p.password, v.subscribe_acl, v.publish_acl " +
                                    "FROM vmq_auth_acl v " +
                                    "JOIN plain_logins p on v.username = p.username"
                        )
                            .await()
                    storedInfo = result.rows.toList().associate { entry ->
                        Pair(
                            entry.username,
                            MQTTUserInfo(
                                username = entry.username as String,
                                password = entry.password as String,
                                subscribeTopics = (entry.subscribe_acl as JSArray<dynamic>).toList()
                                    .map { (it.pattern as String).replace("%u", entry.username as String) },
                                publishTopics = (entry.subscribe_acl as JSArray<dynamic>).toList()
                                    .map { (it.pattern as String).replace("%u", entry.username as String) }
                            )
                        )
                    }
                    storedUserInfos = storedInfo
                    return storedInfo!!
                } catch (ex: dynamic) {
                    console.error(
                        "The following exception occurred while requesting mqtt-credentials from postgres, using credentials from config",
                        ex
                    )
                    return mapOf(
                        Config.MQTT.tutorUser to MQTTUserInfo(
                            Config.MQTT.tutorUser, Config.MQTT.tutorPassword,
                            listOf("#"), listOf("#")
                        )
                    )
                }

            }
        }
    }

    data class MQTTUserInfo(
        override val username: String,
        override val password: String,
        val subscribeTopics: List<String>,
        val publishTopics: List<String>,
    ) : ICredentialProvider
}
