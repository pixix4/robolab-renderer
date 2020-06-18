package de.robolab.server.net

import de.robolab.common.net.HttpStatusCode
import de.robolab.common.utils.BuildInformation
import de.robolab.server.externaljs.body_parser.json
import de.robolab.server.externaljs.body_parser.text
import de.robolab.server.externaljs.createIO
import de.robolab.server.externaljs.emptyDynamic
import de.robolab.server.externaljs.express.*
import de.robolab.server.externaljs.http.createServer
import de.robolab.server.externaljs.toJSArray
import de.robolab.server.routes.BeverageRouter
import de.robolab.server.routes.InfoRouter
import de.robolab.server.routes.PlanetRouter
import de.robolab.server.routes.logoResponse

object DefaultEnvironment {
    val app: ExpressApp = createApp()
    val http: dynamic = createServer(app)
    val io: dynamic = createIO(http)

    fun createApiRouter() : Router {
        val router = createRouter()
        router.use("/") { _, res, next ->
            res.setHeader("Access-Control-Allow-Origin", "*")
            next(null)
        }
        router.use(json())
        router.use(text())
        router.use("/tea", BeverageRouter.teaRouter)
        router.use("/coffee", BeverageRouter.coffeeRouter)
        router.use("/mate", BeverageRouter.mateRouter)
        router.use("/planets", PlanetRouter.router)
        router.use("/info", InfoRouter.router)
        router.get("/", logoResponse)

        router.get("/version") { _, res ->
            res.status(HttpStatusCode.Ok)
            res.format("json" to {
                val obj = emptyDynamic()
                obj["version"] = BuildInformation.versionServer
                obj["versionString"] = BuildInformation.versionServer.toString()
                res.send(JSON.stringify(obj))
            },"text" to {
                res.send(BuildInformation.versionServer.toString())
            })
        }

        return router
    }
}