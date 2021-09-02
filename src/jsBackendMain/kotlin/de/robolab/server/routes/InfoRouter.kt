@file:Suppress("USELESS_CAST")

package de.robolab.server.routes

import de.robolab.client.net.requests.PlanetJsonInfo
import de.robolab.common.auth.requireTutor
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType
import de.robolab.common.utils.BuildInformation
import de.robolab.common.utils.encode
import de.robolab.server.config.Config
import de.robolab.server.externaljs.express.*
import de.robolab.common.jsutils.toDynamic
import de.robolab.server.model.asPlanetJsonInfo
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Suppress("ConstantConditionIf")
object InfoRouter {
    val router: DefaultRouter = createRouter()

    init {
        router.getSuspend("/exam") { req, res ->
            req.user.requireTutor()
            val result: JsonObject = buildJsonObject {
                put("isExam", Config.Info.examEnabled)
                if (Config.Info.examEnabled) {
                    this.put("planets", buildJsonArray {
                        for (nameIdPair in Config.Info.examPlanets.split(";")) {
                            if ('=' in nameIdPair) {
                                val (name, id) = nameIdPair.split("=", limit = 2)
                                add(buildJsonObject {
                                    put("name", name)
                                    put("info", PlanetJsonInfo.serializer()
                                        .encode(PlanetRouter.planetStore.getInfo(id)?.asPlanetJsonInfo()
                                            ?: throw NullPointerException("Planet $id does not exists!")))
                                })
                            }
                        }
                    })
                }
            }
            res.setHeader("content-type", "application/json")
            res.status(200).send(result.toString())
        }

        router.getSuspend("/flushdbyesreallyiknowwhatimdoing") { req, res ->
            req.user.requireTutor()
            res.setHeader("content-type", "text/plain")
            val response = PlanetRouter.clearMeta()
            res.status(if (response.first) HttpStatusCode.Ok else HttpStatusCode.InternalServerError)
                .send(response.second)
        }

        router.getSuspend("/whoami") { req, res ->
            res.formatReceiving(
                MIMEType.JSON to {
                    res.status(200).sendSerializable(req.user)
                },
                MIMEType.PlainText to {
                    res.status(200).send(req.user.toString())
                },
            )
        }

        router.getSuspend("/commit") { _, res ->
            res.formatReceiving(
                MIMEType.PlainText to {
                    res.sendFile("./build.ini")
                },
                MIMEType.JSON to {
                    res.status(200).send(BuildInformation.dataMap.associate { (groupName, group) ->
                        groupName to group.associate { (key, value) ->
                            key to value.value.toString()
                        }.toDynamic()
                    }.toDynamic() as Any?)
                },
            )
        }
    }
}
