package de.robolab.server.routes

import de.robolab.server.config.Config
import de.robolab.server.externaljs.express.DefaultRouter
import de.robolab.server.externaljs.express.createRouter
import de.robolab.common.jsutils.jsTruthy

object BeverageRouter {
    val mateRouter: DefaultRouter = createRouter()
    val teaRouter: DefaultRouter = createRouter()
    val coffeeRouter: DefaultRouter = createRouter()

    init {
        mateRouter.get("/") { _, res ->
            val targetURL: String? = Config.Beverage.payPalMateURL
            if (targetURL.jsTruthy())
                res.redirect(302, targetURL!!)
            else {
                res.setHeader("content-type","text/plain")
                res.status(402).send(Config.Beverage.payPalMateText)
            }
        }
        teaRouter.get("/") { _, res ->
            res.sendStatus(501)
        }
        coffeeRouter.get("/") { _, res ->
            res.sendStatus(418)
        }
    }
}