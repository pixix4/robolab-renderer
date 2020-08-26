package de.robolab.server.routes

import de.robolab.common.net.HttpStatusCode
import de.robolab.server.config.Config
import de.robolab.server.data.FilePlanetStore
import de.robolab.server.data.RedisPlanetMetaStore
import de.robolab.server.externaljs.express.DefaultRouter
import de.robolab.server.externaljs.express.createRouter
import de.robolab.server.externaljs.express.getSuspend
import de.robolab.server.externaljs.express.status
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Suppress("ConstantConditionIf")
object InfoRouter {
    val router: DefaultRouter = createRouter()

    init {
        router.get("/exam") { _, res ->
            val result: JsonObject = buildJsonObject {
                put("isExam", Config.Info.examEnabled)
                if (Config.Info.examEnabled) {
                    put("smallPlanetID", Config.Info.examPlanetSmallID)
                    put("smallPlanetName", Config.Info.examPlanetSmallName)
                    put("largePlanetID", Config.Info.examPlanetLargeID)
                    put("largePlanetName", Config.Info.examPlanetLargeName)
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
    }
}