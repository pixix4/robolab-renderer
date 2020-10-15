@file:Suppress("USELESS_CAST")

package de.robolab.server.routes

import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType
import de.robolab.server.auth.requireTutor
import de.robolab.server.config.Config
import de.robolab.server.externaljs.dynamicOf
import de.robolab.server.externaljs.express.*

object MQTTRouter {
    val router: DefaultRouter = createRouter()

    init {
        router.getSuspend("/credentials") { req, res ->
            req.user.requireTutor()
            res.formatReceiving(MIMEType.PlainText to {
                status(HttpStatusCode.Ok).send(Config.MQTT.tutorUser+":"+Config.MQTT.tutorPassword)
            }, MIMEType.JSON to {
                status(HttpStatusCode.Ok).send(
                    dynamicOf(
                        "username" to Config.MQTT.tutorUser,
                        "password" to Config.MQTT.tutorPassword
                    ) as Any?
                )
            })
        }
    }
}