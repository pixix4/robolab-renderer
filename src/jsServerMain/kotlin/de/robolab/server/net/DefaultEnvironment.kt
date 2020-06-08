package de.robolab.server.net

import de.robolab.server.externaljs.express.ExpressApp
import de.robolab.server.externaljs.express.createApp
import de.robolab.server.externaljs.createIO
import de.robolab.server.externaljs.express.Router
import de.robolab.server.externaljs.express.createRouter
import de.robolab.server.externaljs.http.createServer
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
        router.use("/"){_,res,next->
            res.setHeader("Access-Control-Allow-Origin","*")
            next(null)
        }
        router.use("/tea", BeverageRouter.teaRouter)
        router.use("/coffee", BeverageRouter.coffeeRouter)
        router.use("/mate", BeverageRouter.mateRouter)
        router.use("/planets", PlanetRouter.router)
        router.use("/info", InfoRouter.router)
        router.get("/", logoResponse)
        return router
    }
}