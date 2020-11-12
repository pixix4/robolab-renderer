@file:Suppress("USELESS_CAST")

package de.robolab.server.routes

import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType
import de.robolab.server.auth.*
import de.robolab.server.config.Config
import de.robolab.server.data.PostgresMQTTCredentialStore
import de.robolab.server.externaljs.dynamicOf
import de.robolab.server.externaljs.express.*
import de.robolab.server.externaljs.toJSArray
import de.robolab.server.net.RESTResponseCodeException
import de.robolab.server.routes.MQTTRouter.defaultMQTTUsername
import de.robolab.server.routes.MQTTRouter.requireAccessForMQTTUsername

object MQTTRouter {

    val User.defaultMQTTUsername: String
        get() = if (hasTutorAccess) "tutor" else "RANDOM CONSTANT NAME WHICH IS NOT A VALID GROUP-ID"

    fun User.requireAccessForMQTTUsername(username: String) {
        if (username != defaultMQTTUsername)
            requireTutor()
        else
            requireLogin()
    }

    val router: DefaultRouter = createRouter()

    suspend fun obtainUserInfo(user: User, username: String? = null): PostgresMQTTCredentialStore.MQTTUserInfo {
        val requestedUserName: String = username ?: user.defaultMQTTUsername
        user.requireAccessForMQTTUsername(requestedUserName)
        val userInfos = PostgresMQTTCredentialStore.getMQTTUserInfos()
        return userInfos[requestedUserName] ?: throw RESTResponseCodeException(
            HttpStatusCode.NotFound,
            "Could not find credentials for user \"$requestedUserName\""
        )
    }

    init {
        router.getSuspend("/urls") { _, res ->
            res.formatReceiving(MIMEType.JSON to {
                status(HttpStatusCode.Ok).send(
                    dynamicOf(
                        "wss" to Config.MQTT.mothershipURLWSS,
                        "ssl" to Config.MQTT.mothershipURLSSL,
                        "log" to Config.MQTT.mothershipURLLog,
                    ) as Any?
                )
            })
        }
        router.getSuspend("/credentials") { req, res ->
            req.user.requireLogin()
            val mqttInfo = obtainUserInfo(req.user, req.query.username as? String)

            res.formatReceiving(MIMEType.PlainText to {
                status(HttpStatusCode.Ok).send(mqttInfo.username + ":" + mqttInfo.password)
            }, MIMEType.JSON to {
                status(HttpStatusCode.Ok).send(
                    dynamicOf(
                        "username" to mqttInfo.username,
                        "password" to mqttInfo.password
                    ) as Any?
                )
            })
        }
        router.getSuspend("/topics") { req, res ->
            req.user.requireLogin()
            val mqttInfo = obtainUserInfo(req.user, req.query.username as? String)

            res.formatReceiving(MIMEType.PlainText to {
                status(HttpStatusCode.Ok).send(
                    ("---SUBSCRIBE---\n" +
                            mqttInfo.subscribeTopics.joinToString("\n") { "\t$it" } +
                            "\n---PUBLISH---\n" +
                            mqttInfo.publishTopics.joinToString("\n") { "\t$it" }).replace("\n\n", "\n")
                )
            }, MIMEType.JSON to {
                status(HttpStatusCode.Ok).send(
                    dynamicOf(
                        "subscribe" to mqttInfo.subscribeTopics.toJSArray(),
                        "publish" to mqttInfo.publishTopics.toJSArray()
                    ) as Any?
                )
            })
        }
        router.getSuspend("/connection") { req, res ->
            req.user.requireLogin()
            val mqttInfo = obtainUserInfo(req.user, req.query.username as? String)

            res.formatReceiving(MIMEType.PlainText to {
                status(HttpStatusCode.Ok).send(
                    (mqttInfo.username + ":" + mqttInfo.password +
                            "\n---SUBSCRIBE---\n" + mqttInfo.subscribeTopics.joinToString("\n") { "\t$it" } +
                            "\n---PUBLISH---\n" +
                            mqttInfo.publishTopics.joinToString("\n") { "\t$it" }).replace("\n\n", "\n")
                )
            }, MIMEType.JSON to {
                status(HttpStatusCode.Ok).send(
                    dynamicOf(
                        "username" to mqttInfo.username,
                        "password" to mqttInfo.password,
                        "subscribe" to mqttInfo.subscribeTopics.toJSArray(),
                        "publish" to mqttInfo.publishTopics.toJSArray()
                    ) as Any?
                )
            })
        }
    }
}