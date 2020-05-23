package de.robolab.server

import de.robolab.server.net.DefaultEnvironment
import de.robolab.server.routes.BeverageRouter
import de.robolab.server.routes.InfoRouter
import de.robolab.server.routes.PlanetRouter

fun main() {
    DefaultEnvironment.app.use("/tea", BeverageRouter.teaRouter)
    DefaultEnvironment.app.use("/coffee", BeverageRouter.coffeeRouter)
    DefaultEnvironment.app.use("/mate", BeverageRouter.mateRouter)
    DefaultEnvironment.app.use("/planets", PlanetRouter.router)
    DefaultEnvironment.app.use("/info", InfoRouter.router)
    DefaultEnvironment.app.get("/") { _, res ->
        res.status(200).send("Hello world!")
    }
    DefaultEnvironment.http.listen(8080) {
        console.log("listening on port 8080")
    }
    console.log("\n\n\tHello, Kotlin/JS!\n\n")
}