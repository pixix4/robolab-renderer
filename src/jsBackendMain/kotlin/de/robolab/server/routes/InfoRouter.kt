@file:Suppress("USELESS_CAST")

package de.robolab.server.routes

import de.robolab.client.net.requests.PlanetJsonInfo
import de.robolab.common.auth.requireTutor
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType
import de.robolab.common.utils.BuildInformation
import de.robolab.common.utils.encode
import de.robolab.server.config.Config
import de.robolab.server.config.getLargeExamPlanetInfo
import de.robolab.server.config.getSmallExamPlanetInfo
import de.robolab.server.externaljs.express.*
import de.robolab.common.jsutils.toDynamic
import kotlinx.serialization.json.JsonObject
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
                    put("smallPlanet", PlanetJsonInfo.serializer().encode(PlanetRouter.planetStore.getSmallExamPlanetInfo()))
                    put("largePlanet", PlanetJsonInfo.serializer().encode(PlanetRouter.planetStore.getLargeExamPlanetInfo()))
                }
            }
            res.setHeader("content-type", "application/json")
            res.status(200).send(result.toString())
        }

        router.getSuspend("/flushdbyesreallyiknowwhatimdoing"){ req, res->
            req.user.requireTutor()
            res.setHeader("content-type","text/plain")
            val response = PlanetRouter.clearMeta()
            res.status(if(response.first) HttpStatusCode.Ok else HttpStatusCode.InternalServerError).send(response.second)
        }

        router.getSuspend("/whoami"){ req, res ->
            res.formatReceiving(
                MIMEType.JSON to {
                    res.status(200).sendSerializable(req.user)
                },
                MIMEType.PlainText to {
                    res.status(200).send(req.user.toString())
                },
            )
        }

        router.getSuspend("/commit"){_, res ->
            res.formatReceiving(
                MIMEType.PlainText to {
                    res.sendFile("./build.ini")
                },
                MIMEType.JSON to {
                    res.status(200).send(BuildInformation.dataMap.associate {(groupName, group)->
                        groupName to group.associate { (key, value) ->
                            key to value.value.toString()
                        }.toDynamic()
                    }.toDynamic() as Any?)
                },
            )
        }
    }
}