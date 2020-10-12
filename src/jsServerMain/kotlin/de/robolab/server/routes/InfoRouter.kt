package de.robolab.server.routes

import de.robolab.client.net.requests.PlanetJsonInfo
import de.robolab.common.net.HttpStatusCode
import de.robolab.common.net.MIMEType
import de.robolab.common.utils.encode
import de.robolab.server.auth.User
import de.robolab.server.config.Config
import de.robolab.server.config.getLargeExamPlanetInfo
import de.robolab.server.config.getSmallExamPlanetInfo
import de.robolab.server.data.FilePlanetStore
import de.robolab.server.data.RedisPlanetMetaStore
import de.robolab.server.externaljs.express.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Suppress("ConstantConditionIf")
object InfoRouter {
    val router: DefaultRouter = createRouter()

    init {
        router.getSuspend("/exam") { _, res ->
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

        router.getSuspend("/flushdbyesreallyiknowwhatimdoing"){ _, res->
            res.setHeader("content-type","text/plain")
            val response = PlanetRouter.clearMeta()
            res.status(if(response.first) HttpStatusCode.Ok else HttpStatusCode.InternalServerError).send(response.second)
        }

        router.getSuspend("/whoami"){ req, res ->
            res.formatReceiving(
                MIMEType.JSON to {
                    res.status(200).send(req.user.toJSON() as Any)
                },
                MIMEType.PlainText to {
                    res.status(200).send(req.user.internalName)
                },
            )
        }
    }
}