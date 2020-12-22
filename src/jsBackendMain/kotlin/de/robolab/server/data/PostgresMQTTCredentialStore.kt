package de.robolab.server.data

import de.robolab.client.net.requests.mqtt.MQTTConnectionInfo
import de.robolab.server.config.Config
import de.robolab.server.externaljs.JSArray
import de.robolab.server.externaljs.pg.Client
import de.robolab.server.externaljs.pg.obtainClient
import de.robolab.server.externaljs.pg.withConnection
import de.robolab.server.externaljs.toList
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.await

object PostgresMQTTCredentialStore {
    val client: Client = obtainClient(Config.MQTT.database)


    private val connectionRLock: ReentrantLock = ReentrantLock()

    private var storedUserInfos: Map<String, MQTTConnectionInfo>? = null


    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    suspend fun getMQTTUserInfos(): Map<String, MQTTConnectionInfo> {
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
                            MQTTConnectionInfo(
                                username = entry.username as String,
                                password = entry.password as String,
                                subscribeTopicPatterns = (entry.subscribe_acl as JSArray<dynamic>).toList()
                                    .map { (it.pattern as String).replace("%u", entry.username as String) },
                                publishTopicPatterns = (entry.subscribe_acl as JSArray<dynamic>).toList()
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
                        Config.MQTT.tutorUser to MQTTConnectionInfo(
                            username = Config.MQTT.tutorUser,
                            password = Config.MQTT.tutorPassword,
                            subscribeTopicPatterns = listOf("#"),
                            publishTopicPatterns = listOf("#")
                        )
                    )
                }

            }
        }
    }
}
