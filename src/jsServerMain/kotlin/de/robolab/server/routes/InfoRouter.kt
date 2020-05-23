package de.robolab.server.routes

import de.robolab.server.config.Config
import de.robolab.server.externaljs.express.Router
import de.robolab.server.externaljs.express.createRouter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlin.js.json

@Suppress("ConstantConditionIf")
object InfoRouter {
    val router: Router = createRouter()

    init {
        router.get("/exam") { _, res ->
            val result: JsonObject = kotlinx.serialization.json.json {
                "isExam" to Config.Info.examEnabled
                if (Config.Info.examEnabled) {
                    "smallPlanetID" to Config.Info.examPlanetSmallID
                    "smallPlanetName" to Config.Info.examPlanetSmallName
                    "largePlanetID" to Config.Info.examPlanetLargeID
                    "largePlanetName" to Config.Info.examPlanetLargeName
                }
            }
            res.status(200).send(result.toString())
        }
    }
}